package es.tecnalia.ittxartela.ws.server.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.map.xml_schemas.IntermediacionOnlineAsyncPortType;
import es.map.xml_schemas.IntermediacionOnlinePortType;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Consulta;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.DatosEspecificosItTxartela;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.EstadoResultado;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Retorno;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Traza;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.Peticion;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Estado;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Respuesta;
import es.tecnalia.ittxartela.ws.server.constant.EstadoPeticionAsincrona;
import es.tecnalia.ittxartela.ws.server.model.Audit;
import es.tecnalia.ittxartela.ws.server.model.AsyncPeticion;
import es.tecnalia.ittxartela.ws.server.repository.AuditRepository;
import es.tecnalia.ittxartela.ws.server.repository.AsyncPeticionRepository;
import es.tecnalia.ittxartela.ws.server.util.PeticionValidator;
import es.tecnalia.ittxartela.ws.server.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ITTxartelaOnlineServiceImpl implements IntermediacionOnlinePortType, IntermediacionOnlineAsyncPortType {

    private static final Duration TIEMPO_RESPUESTA_POR_DEFECTO = Duration.ofDays(5);
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private PeticionValidator peticionValidator;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AsyncPeticionRepository asyncPeticionRepository;

    // --- Síncrono ---
    @Override
    @Transactional
    public Respuesta peticionSincrona(Peticion peticion) {
        log.info("peticionSincrona recibida {}", XmlUtil.toXml(peticion));

        Audit audit = crearRegistroAuditoria(peticion, EstadoPeticionAsincrona.DISPONIBLE, LocalDateTime.now());

        Respuesta respuesta = generarRespuestaValidada(peticion);

        audit.setXmlRespuesta(XmlUtil.toXml(respuesta));
        audit.setMensajeEstado(EstadoPeticionAsincrona.DISPONIBLE.getDescripcion());
        audit.setFechaDisponible(LocalDateTime.now());
        audit.setDetalleError(null);
        auditRepository.save(audit);

        log.info("peticionSincrona respuesta generada {}", XmlUtil.toXml(respuesta));

        return respuesta;
    }

    // --- Asíncrono ---
    @Override
    @Transactional
    public Respuesta peticionAsincrona(Peticion peticion) {
        log.info("peticionAsincrona recibida {}", XmlUtil.toXml(peticion));

        Optional<PeticionValidator.RespuestaError> error = peticionValidator.validar(peticion);

        if (error.isPresent()) {
            Audit audit = crearRegistroAuditoria(peticion, EstadoPeticionAsincrona.ERROR, LocalDateTime.now());

            Respuesta respuestaError = construirRespuestaBase(peticion);
            aplicarError(respuestaError, error.get());
            asegurarEstado(respuestaError).setTiempoEstimadoRespuesta(0);

            audit.setMensajeEstado("Petición rechazada: " + error.get().getMensaje());
            audit.setDetalleError(error.get().getMensaje());
            audit.setXmlRespuesta(XmlUtil.toXml(respuestaError));
            audit.setFechaDisponible(LocalDateTime.now());
            auditRepository.save(audit);

            return respuestaError;
        }

        Audit audit = crearRegistroAuditoria(
                peticion,
                EstadoPeticionAsincrona.RECIBIDA,
                LocalDateTime.now().plus(TIEMPO_RESPUESTA_POR_DEFECTO)
        );

        Respuesta respuesta = construirRespuestaBase(peticion);
        Estado estado = asegurarEstado(respuesta);
        estado.setCodigoEstado(EstadoPeticionAsincrona.RECIBIDA.getCodigo());
        estado.setLiteralError(EstadoPeticionAsincrona.RECIBIDA.getDescripcion());
        establecerTiempoEstimadoRespuesta(estado, TIEMPO_RESPUESTA_POR_DEFECTO);

        asegurarTransmisionDatos(respuesta).setDatosEspecificos(crearDatosEstado(peticion, audit));

        audit.setXmlRespuesta(XmlUtil.toXml(respuesta));
        auditRepository.save(audit);

        guardarAsyncPeticion(peticion, respuesta, EstadoPeticionAsincrona.RECIBIDA, estado.getTiempoEstimadoRespuesta());

        log.info("peticionAsincrona {} almacenada con estado {}", peticion.getAtributos().getIdPeticion(),
                EstadoPeticionAsincrona.RECIBIDA.getDescripcion());

        return respuesta;
    }

    // --- Procesamiento diferido ---
    @Transactional
    @Scheduled(fixedDelayString = "${ittxartela.async.polling-interval-ms:60000}")
    public void procesarPeticionesAsincronasPendientes() {
        List<AsyncPeticion> pendientes = asyncPeticionRepository
                .findByEstadoOrderByIdAsc(EstadoPeticionAsincrona.RECIBIDA.getCodigo());

        for (AsyncPeticion async : pendientes) {
            try {
                Audit audit = auditRepository.findTopByIdPeticionOrderByIdDesc(async.getIdPeticion());
                Peticion peticion = XmlUtil.fromXml(async.getXmlPeticion(), Peticion.class);

                Respuesta respuestaFinal = generarRespuestaValidada(peticion);

                if (audit != null) {
                    audit.setXmlRespuesta(XmlUtil.toXml(respuestaFinal));
                    audit.setEstado(EstadoPeticionAsincrona.DISPONIBLE.getCodigo());
                    audit.setMensajeEstado(EstadoPeticionAsincrona.DISPONIBLE.getDescripcion());
                    audit.setDetalleError(null);
                    audit.setFechaDisponible(LocalDateTime.now());
                    auditRepository.save(audit);
                }

                guardarAsyncPeticion(peticion, respuestaFinal, EstadoPeticionAsincrona.DISPONIBLE, 0);

                log.info("Petición asincrona {} procesada correctamente", async.getIdPeticion());

            } catch (Exception e) {
                log.error("Error procesando la petición asincrona {}", async.getIdPeticion(), e);
                async.setEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
                async.setTer(0);
                asyncPeticionRepository.save(async);
            }
        }
    }

    // --- Consultar asíncrono ---
    @Override
    @Transactional
    public Respuesta consultarPeticionAsincrona(Peticion peticionConsulta) {
        log.info("consultarPeticionAsincrona recibida {}",
                peticionConsulta != null ? XmlUtil.toXml(peticionConsulta) : "<peticion nula>");

        if (peticionConsulta == null || peticionConsulta.getAtributos() == null
                || peticionConsulta.getAtributos().getIdPeticion() == null
                || peticionConsulta.getAtributos().getIdPeticion().isBlank()) {
            throw new IllegalArgumentException("El identificador de petición es obligatorio");
        }

        String idPeticion = peticionConsulta.getAtributos().getIdPeticion();
        AsyncPeticion asyncPeticion = asyncPeticionRepository.findByIdPeticion(idPeticion);

        if (asyncPeticion == null) {
            throw new IllegalArgumentException("No se ha encontrado la petición " + idPeticion);
        }

        Audit audit = auditRepository.findTopByIdPeticionOrderByIdDesc(idPeticion);

        // Caso DISPONIBLE
        if (EstadoPeticionAsincrona.DISPONIBLE.matches(asyncPeticion.getEstado())
                && asyncPeticion.getXmlRespuesta() != null) {
            Respuesta respuestaAlmacenada = reconstruirRespuesta(asyncPeticion.getXmlRespuesta(), idPeticion);
            if (respuestaAlmacenada != null) {
                return respuestaAlmacenada;
            }
        }

        // Caso RECIBIDA
        Peticion peticion = reconstruirPeticion(asyncPeticion, audit, idPeticion);
        Respuesta respuesta = construirRespuestaBase(peticion);
        Estado estado = asegurarEstado(respuesta);

        if (EstadoPeticionAsincrona.RECIBIDA.matches(asyncPeticion.getEstado())) {
            estado.setCodigoEstado(asyncPeticion.getEstado());
            estado.setLiteralError("En proceso");
            estado.setTiempoEstimadoRespuesta(asyncPeticion.getTer());
            return respuesta;
        }

        // Caso ERROR
        if (EstadoPeticionAsincrona.ERROR.matches(asyncPeticion.getEstado())) {
            estado.setCodigoEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
            estado.setLiteralError("Error en la petición");
            estado.setTiempoEstimadoRespuesta(0);
            return respuesta;
        }

        throw new IllegalArgumentException("Estado desconocido para la petición " + idPeticion);
    }

    // --- Helpers ---
    private void guardarAsyncPeticion(Peticion peticion, Respuesta respuesta, EstadoPeticionAsincrona estado,
                                      Integer tiempoEstimadoRespuesta) {
        Objects.requireNonNull(peticion, "La petición no puede ser nula");

        String idPeticion = peticion.getAtributos().getIdPeticion();
        AsyncPeticion asyncPeticion = asyncPeticionRepository.findByIdPeticion(idPeticion);

        if (asyncPeticion == null) {
            asyncPeticion = new AsyncPeticion();
            asyncPeticion.setIdPeticion(idPeticion);
        }

        asyncPeticion.setXmlPeticion(XmlUtil.toXml(peticion));
        asyncPeticion.setEstado(estado.getCodigo());
        if (tiempoEstimadoRespuesta != null) {
            asyncPeticion.setTer(tiempoEstimadoRespuesta);
        }
        if (respuesta != null) {
            asyncPeticion.setXmlRespuesta(XmlUtil.toXml(respuesta));
        }
        asyncPeticionRepository.save(asyncPeticion);
    }

    private Respuesta reconstruirRespuesta(String xmlRespuesta, String idPeticion) {
        if (xmlRespuesta == null || xmlRespuesta.isBlank()) return null;
        try {
            return XmlUtil.fromXml(xmlRespuesta, Respuesta.class);
        } catch (Exception e) {
            log.error("No ha sido posible reconstruir la respuesta {}", idPeticion, e);
            return null;
        }
    }

    private Peticion reconstruirPeticion(AsyncPeticion asyncPeticion, Audit audit, String idPeticion) {
        String xmlPeticion = asyncPeticion.getXmlPeticion();
        if ((xmlPeticion == null || xmlPeticion.isBlank()) && audit != null) {
            xmlPeticion = audit.getXmlPeticion();
        }
        if (xmlPeticion == null || xmlPeticion.isBlank()) {
            throw new IllegalArgumentException("No existe petición almacenada para " + idPeticion);
        }
        return XmlUtil.fromXml(xmlPeticion, Peticion.class);
    }

    // TODO: implementar crearRegistroAuditoria, aplicarError, aplicarRespuestaCorrecta, asegurarEstado, etc.
    // (se asume que ya están en tu clase original).
}
