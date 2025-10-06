package es.tecnalia.ittxartela.ws.server.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.map.xml_schemas.IntermediacionOnlineAsyncPortType;
import es.map.xml_schemas.IntermediacionOnlinePortType;
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

    // --- Servicio SINCRONO ---
    @Override
    @Transactional
    public Respuesta peticionSincrona(Peticion peticion) {
        log.info("Petición SINCRONA recibida {}", XmlUtil.toXml(peticion));

        Audit audit = crearRegistroAuditoria(peticion, EstadoPeticionAsincrona.DISPONIBLE, LocalDateTime.now());
        Respuesta respuesta = generarRespuestaValidada(peticion);

        audit.setXmlRespuesta(XmlUtil.toXml(respuesta));
        audit.setEstado(EstadoPeticionAsincrona.DISPONIBLE.getCodigo());
        audit.setMensajeEstado(EstadoPeticionAsincrona.DISPONIBLE.getDescripcion());
        audit.setFechaDisponible(LocalDateTime.now());
        auditRepository.save(audit);

        log.info("Petición SINCRONA procesada correctamente");
        return respuesta;
    }

    // --- Servicio ASINCRONO ---
    @Override
    @Transactional
    public Respuesta peticionAsincrona(Peticion peticion) {
        log.info("Petición ASINCRONA recibida {}", XmlUtil.toXml(peticion));

        Optional<PeticionValidator.RespuestaError> error = peticionValidator.validar(peticion);

        if (error.isPresent()) {
            Respuesta respuestaError = construirRespuestaBase(peticion);
            aplicarError(respuestaError, peticion, error.get());
            guardarAsyncPeticion(peticion, respuestaError, EstadoPeticionAsincrona.ERROR, 0);
            return respuestaError;
        }

        Audit audit = crearRegistroAuditoria(
            peticion, EstadoPeticionAsincrona.RECIBIDA,
            LocalDateTime.now().plus(TIEMPO_RESPUESTA_POR_DEFECTO)
        );

        Respuesta respuesta = construirRespuestaBase(peticion);
        Estado estado = asegurarEstado(respuesta);
        estado.setCodigoEstado(EstadoPeticionAsincrona.RECIBIDA.getCodigo());
        estado.setLiteralError(EstadoPeticionAsincrona.RECIBIDA.getDescripcion());

        audit.setXmlRespuesta(XmlUtil.toXml(respuesta));
        auditRepository.save(audit);
        guardarAsyncPeticion(peticion, respuesta, EstadoPeticionAsincrona.RECIBIDA, (int) TIEMPO_RESPUESTA_POR_DEFECTO.toMinutes());

        log.info("Petición ASINCRONA almacenada y acuse generado");
        return respuesta;
    }

    // --- CONSULTA ASINCRONA ---
    @Override
    @Transactional
    public Respuesta consultarPeticionAsincrona(Peticion peticion) {
        String idPeticion = peticion.getAtributos().getIdPeticion();
        log.info("Consulta ASINCRONA recibida para {}", idPeticion);

        AsyncPeticion asyncPeticion = asyncPeticionRepository.findByIdPeticion(idPeticion);
        if (asyncPeticion == null) {
            throw new IllegalArgumentException("No se encontró la petición " + idPeticion);
        }

        Respuesta respuesta = XmlUtil.fromXml(asyncPeticion.getXmlRespuesta(), Respuesta.class);
        if (respuesta == null) {
            respuesta = construirRespuestaBase(peticion);
            Estado estado = asegurarEstado(respuesta);
            estado.setCodigoEstado(EstadoPeticionAsincrona.RECIBIDA.getCodigo());
            estado.setLiteralError("Procesando petición asíncrona...");
        }

        return respuesta;
    }

    // --- Métodos auxiliares ---

    private Audit crearRegistroAuditoria(Peticion peticion, EstadoPeticionAsincrona estado, LocalDateTime fechaPrevista) {
        Audit audit = new Audit();
        audit.setIdPeticion(peticion.getAtributos().getIdPeticion());
        audit.setXmlPeticion(XmlUtil.toXml(peticion));
        audit.setEstado(estado.getCodigo());
        audit.setMensajeEstado(estado.getDescripcion());
        audit.setFechaRespuestaPrevista(fechaPrevista);
        return auditRepository.save(audit);
    }

    private Respuesta generarRespuestaValidada(Peticion peticion) {
        Respuesta respuesta = construirRespuestaBase(peticion);
        Optional<PeticionValidator.RespuestaError> error = peticionValidator.validar(peticion);
        if (error.isPresent()) {
            aplicarError(respuesta, peticion, error.get());
        }
        return respuesta;
    }

    private Respuesta construirRespuestaBase(Peticion peticion) {
        Respuesta respuesta = mapper.map(peticion, Respuesta.class);
        if (respuesta.getAtributos() == null) {
            respuesta.setAtributos(new es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Atributos());
        }
        respuesta.getAtributos().setTimeStamp(XmlUtil.obtenerFechaHoraXml());
        asegurarEstado(respuesta);
        return respuesta;
    }

    private Estado asegurarEstado(Respuesta respuesta) {
        if (respuesta.getAtributos().getEstado() == null) {
            respuesta.getAtributos().setEstado(new Estado());
        }
        return respuesta.getAtributos().getEstado();
    }

    private void aplicarError(Respuesta respuesta, Peticion peticion, PeticionValidator.RespuestaError error) {
        Estado estado = asegurarEstado(respuesta);
        estado.setCodigoEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
        estado.setLiteralError(error.getMensaje());
        estado.setTiempoEstimadoRespuesta(0);
    }

    private void guardarAsyncPeticion(Peticion peticion, Respuesta respuesta, EstadoPeticionAsincrona estado, int ter) {
        AsyncPeticion async = new AsyncPeticion();
        async.setIdPeticion(peticion.getAtributos().getIdPeticion());
        async.setXmlPeticion(XmlUtil.toXml(peticion));
        async.setXmlRespuesta(XmlUtil.toXml(respuesta));
        async.setEstado(estado.getCodigo());
        async.setTer(ter);
        asyncPeticionRepository.save(async);
    }
}
