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
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.SolicitudTransmision;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Atributos;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Estado;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Respuesta;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.TransmisionDatos;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Transmisiones;
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
public class ITTxartelaOnlineServiceImpl implements IntermediacionOnlinePortType,
        IntermediacionOnlineAsyncPortType {

    private static final Duration TIEMPO_RESPUESTA_POR_DEFECTO = Duration.ofDays(5);
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired private ModelMapper mapper;
    @Autowired private PeticionValidator peticionValidator;
    @Autowired private AuditRepository auditRepository;
    @Autowired private AsyncPeticionRepository asyncPeticionRepository;

    // synchronous request
    @Override
    @Transactional
    public Respuesta peticionSincrona(Peticion peticion) {
        log.info("peticionSincrona recibida {}", XmlUtil.toXml(peticion));

        Audit audit = crearRegistroAuditoria(
                peticion, EstadoPeticionAsincrona.DISPONIBLE, LocalDateTime.now());

        Respuesta respuesta = generarRespuestaValidada(peticion);
        audit.setXmlRespuesta(XmlUtil.toXml(respuesta));

        LocalDateTime ahora = LocalDateTime.now();
        Estado estadoResp = (respuesta.getAtributos() != null)
                ? respuesta.getAtributos().getEstado() : null;

        // update audit status depending on error/success
        if (estadoResp != null
                && EstadoPeticionAsincrona.ERROR.getCodigo().equals(estadoResp.getCodigoEstado())) {
            audit.setEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
            audit.setMensajeEstado(EstadoPeticionAsincrona.ERROR.getDescripcion());
            audit.setDetalleError(estadoResp.getLiteralError());
        } else {
            audit.setEstado(EstadoPeticionAsincrona.DISPONIBLE.getCodigo());
            audit.setMensajeEstado(EstadoPeticionAsincrona.DISPONIBLE.getDescripcion());
            audit.setDetalleError(null);
        }
        audit.setFechaDisponible(ahora);
        auditRepository.save(audit);

        log.info("peticionSincrona respuesta generada {}", XmlUtil.toXml(respuesta));
        return respuesta;
    }

    // asynchronous request (ack)
    @Override
    @Transactional
    public Respuesta peticionAsincrona(Peticion peticion) {
        log.info("peticionAsincrona recibida {}", XmlUtil.toXml(peticion));

        Optional<PeticionValidator.RespuestaError> error = peticionValidator.validar(peticion);
        if (error.isPresent()) {
            Audit audit = crearRegistroAuditoria(
                    peticion, EstadoPeticionAsincrona.ERROR, LocalDateTime.now());

            Respuesta respError = construirRespuestaBase(peticion);
            aplicarError(respError, peticion, error.get());
            asegurarEstado(respError).setTiempoEstimadoRespuesta(0);

            audit.setMensajeEstado("Petición rechazada: " + error.get().getMensaje());
            audit.setDetalleError(error.get().getMensaje());
            audit.setXmlRespuesta(XmlUtil.toXml(respError));
            audit.setFechaDisponible(LocalDateTime.now());
            auditRepository.save(audit);
            return respError;
        }

        Audit audit = crearRegistroAuditoria(
                peticion,
                EstadoPeticionAsincrona.RECIBIDA,
                LocalDateTime.now().plus(TIEMPO_RESPUESTA_POR_DEFECTO));

        Respuesta acuse = construirRespuestaBase(peticion);
        Estado estado = asegurarEstado(acuse);
        estado.setCodigoEstado(EstadoPeticionAsincrona.RECIBIDA.getCodigo());
        estado.setLiteralError(EstadoPeticionAsincrona.RECIBIDA.getDescripcion());
        establecerTiempoEstimadoRespuesta(estado, TIEMPO_RESPUESTA_POR_DEFECTO);

        asegurarTransmisionDatos(acuse).setDatosEspecificos(crearDatosEstado(peticion, audit));

        audit.setXmlRespuesta(XmlUtil.toXml(acuse));
        auditRepository.save(audit);

        guardarAsyncPeticion(
                peticion, acuse, EstadoPeticionAsincrona.RECIBIDA,
                estado.getTiempoEstimadoRespuesta());

        return acuse;
    }

    // asynchronous poll – this matches the interface signature
    @Override
    @Transactional
    public Respuesta consultarPeticionAsincrona(Peticion peticion) {
        if (peticion == null || peticion.getAtributos() == null
                || peticion.getAtributos().getIdPeticion() == null) {
            throw new IllegalArgumentException("El identificador de petición es obligatorio");
        }

        String id = peticion.getAtributos().getIdPeticion();
        AsyncPeticion async = asyncPeticionRepository.findByIdPeticion(id);
        if (async == null) {
            throw new IllegalArgumentException("No se ha encontrado la petición " + id);
        }

        Audit audit = auditRepository.findTopByIdPeticionOrderByIdDesc(id);

        // available response
        if (EstadoPeticionAsincrona.DISPONIBLE.matches(async.getEstado())
                && async.getXmlRespuesta() != null) {
            Respuesta r = reconstruirRespuesta(async.getXmlRespuesta(), id);
            return r;
        }

        // received/in progress
        if (EstadoPeticionAsincrona.RECIBIDA.matches(async.getEstado())) {
            Peticion original = reconstruirPeticion(async, audit, id);
            Respuesta respuesta = construirRespuestaBase(original);
            Estado estado = asegurarEstado(respuesta);
            estado.setCodigoEstado(EstadoPeticionAsincrona.RECIBIDA.getCodigo());
            estado.setLiteralError(EstadoPeticionAsincrona.RECIBIDA.getDescripcion());
            if (audit != null && audit.getFechaRespuestaPrevista() != null) {
                establecerTiempoEstimadoRespuesta(estado,
                        Duration.between(LocalDateTime.now(), audit.getFechaRespuestaPrevista()));
            } else {
                estado.setTiempoEstimadoRespuesta(async.getTer());
            }
            guardarAsyncPeticion(original, respuesta,
                    EstadoPeticionAsincrona.RECIBIDA, estado.getTiempoEstimadoRespuesta());
            return respuesta;
        }

        // error
        if (EstadoPeticionAsincrona.ERROR.matches(async.getEstado())) {
            Peticion original = reconstruirPeticion(async, audit, id);
            Respuesta r = construirRespuestaBase(original);
            Estado st = asegurarEstado(r);
            st.setCodigoEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
            st.setLiteralError(EstadoPeticionAsincrona.ERROR.getDescripcion());
            st.setTiempoEstimadoRespuesta(0);
            return r;
        }

        throw new IllegalArgumentException("Estado desconocido para la petición " + id);
    }

    // ----------------- helpers -----------------

    private Audit crearRegistroAuditoria(Peticion peticion,
                                         EstadoPeticionAsincrona estadoInicial,
                                         LocalDateTime fechaRespuestaPrevista) {
        Audit audit = new Audit();
        audit.setIdPeticion(obtenerIdPeticion(peticion));
        audit.setXmlPeticion(XmlUtil.toXml(peticion));
        audit.setEstado(estadoInicial.getCodigo());
        audit.setMensajeEstado(estadoInicial.getDescripcion());
        audit.setDetalleError(null);
        audit.setFechaRespuestaPrevista(fechaRespuestaPrevista != null
                ? fechaRespuestaPrevista : LocalDateTime.now());
        return audit;
    }

    private Respuesta generarRespuestaValidada(Peticion peticion) {
        Respuesta r = construirRespuestaBase(peticion);
        Optional<PeticionValidator.RespuestaError> err = peticionValidator.validar(peticion);
        if (err.isPresent()) {
            aplicarError(r, peticion, err.get());
        } else {
            aplicarRespuestaCorrecta(r, peticion);
        }
        return r;
    }

    private Respuesta construirRespuestaBase(Peticion peticion) {
        Respuesta resp = mapper.map(peticion, Respuesta.class);
        if (resp.getAtributos() == null) {
            resp.setAtributos(new Atributos());
        }
        resp.getAtributos().setTimeStamp(XmlUtil.obtenerFechaHoraXml());
        asegurarEstado(resp);
        asegurarTransmisionDatos(resp);
        return resp;
    }

    private void aplicarError(Respuesta resp, Peticion pet, PeticionValidator.RespuestaError error) {
        Estado st = asegurarEstado(resp);
        st.setCodigoEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
        st.setCodigoEstadoSecundario(error.getCodigo());
        st.setLiteralError(error.getMensaje());
        st.setTiempoEstimadoRespuesta(0);
        DatosEspecificosItTxartela de = crearDatosEspecificosComun(
                pet, st.getCodigoEstado(), st.getLiteralError(), null);
        asegurarTransmisionDatos(resp).setDatosEspecificos(de);
    }

    private void aplicarRespuestaCorrecta(Respuesta resp, Peticion pet) {
        Estado st = asegurarEstado(resp);
        st.setCodigoEstado("0000");
        st.setCodigoEstadoSecundario(null);
        st.setLiteralError("Operación realizada correctamente");
        st.setTiempoEstimadoRespuesta(0);
        DatosEspecificosItTxartela de = crearDatosEspecificosComun(
                pet, "0000", "Operación realizada correctamente", null);
        asegurarTransmisionDatos(resp).setDatosEspecificos(de);
    }

    private Estado asegurarEstado(Respuesta resp) {
        if (resp.getAtributos().getEstado() == null) {
            resp.getAtributos().setEstado(new Estado());
        }
        return resp.getAtributos().getEstado();
    }

    private void establecerTiempoEstimadoRespuesta(Estado estado, Duration d) {
        if (estado != null && d != null) {
            long mins = Math.max(0, d.toMinutes());
            estado.setTiempoEstimadoRespuesta((int) Math.min(Integer.MAX_VALUE, mins));
        }
    }

    private TransmisionDatos asegurarTransmisionDatos(Respuesta resp) {
        if (resp.getTransmisiones() == null) {
            resp.setTransmisiones(new Transmisiones());
        }
        if (resp.getTransmisiones().getTransmisionDatos().isEmpty()) {
            resp.getTransmisiones().getTransmisionDatos().add(new TransmisionDatos());
        }
        return resp.getTransmisiones().getTransmisionDatos().get(0);
    }

    private DatosEspecificosItTxartela crearDatosEstado(Peticion peticion, Audit audit) {
        String cod = audit != null && audit.getEstado() != null
                ? audit.getEstado()
                : EstadoPeticionAsincrona.RECIBIDA.getCodigo();
        String desc = audit != null && audit.getMensajeEstado() != null
                ? audit.getMensajeEstado()
                : EstadoPeticionAsincrona.RECIBIDA.getDescripcion();
        LocalDateTime fecha = audit != null ? audit.getFechaRespuestaPrevista() : null;
        return crearDatosEspecificosComun(peticion, cod, desc, fecha);
    }

    private DatosEspecificosItTxartela crearDatosEspecificosComun(Peticion pet,
                                                                  String cod,
                                                                  String desc,
                                                                  LocalDateTime fechaPrevista) {
        DatosEspecificosItTxartela datos = new DatosEspecificosItTxartela();
        Consulta consulta = copiarConsultaDesdePeticion(pet);
        if (consulta == null) {
            consulta = construirConsultaPorDefecto(pet, fechaPrevista);
        }
        if (fechaPrevista != null) {
            consulta.setFechaLimite(fechaPrevista.format(FORMATO_FECHA_HORA));
        } else if (consulta.getFechaLimite() == null || consulta.getFechaLimite().isBlank()) {
            consulta.setFechaLimite(LocalDateTime.now().format(FORMATO_FECHA_HORA));
        }
        if (consulta.getIdTraza() == null || consulta.getIdTraza().isBlank()) {
            consulta.setIdTraza(obtenerIdPeticion(pet));
        }
        datos.setConsulta(consulta);

        Retorno ret = new Retorno();
        Traza traza = new Traza();
        traza.setIdTraza(consulta.getIdTraza());
        traza.setIdPeticion(obtenerIdPeticion(pet));
        ret.setDatosTraza(traza);

        EstadoResultado er = new EstadoResultado();
        er.setResultado(cod);
        er.setDescripcion(desc);
        ret.setEstadoResultado(er);

        datos.setRetorno(ret);
        return datos;
    }

    private Consulta copiarConsultaDesdePeticion(Peticion pet) {
        SolicitudTransmision sol = obtenerPrimeraSolicitud(pet);
        if (sol != null && sol.getDatosEspecificos() != null
                && sol.getDatosEspecificos().getConsulta() != null) {
            Consulta o = sol.getDatosEspecificos().getConsulta();
            Consulta c = new Consulta();
            c.setIdTraza(o.getIdTraza());
            c.setDocumentacion(o.getDocumentacion());
            c.setFechaLimite(o.getFechaLimite());
            c.setTipoCertificacion(o.getTipoCertificacion());
            return c;
        }
        return null;
    }

    private Consulta construirConsultaPorDefecto(Peticion pet, LocalDateTime fechaPrevista) {
        Consulta c = new Consulta();
        c.setIdTraza(obtenerIdPeticion(pet));
        c.setDocumentacion(obtenerDocumentoPrincipal(pet));
        LocalDateTime f = (fechaPrevista != null)
                ? fechaPrevista
                : LocalDateTime.now().plus(TIEMPO_RESPUESTA_POR_DEFECTO);
        c.setFechaLimite(f.format(FORMATO_FECHA_HORA));
        c.setTipoCertificacion(0);
        return c;
    }

    private String obtenerDocumentoPrincipal(Peticion pet) {
        SolicitudTransmision sol = obtenerPrimeraSolicitud(pet);
        if (sol != null && sol.getDatosGenericos() != null
                && sol.getDatosGenericos().getTitular() != null) {
            return Optional.ofNullable(
                    sol.getDatosGenericos().getTitular().getDocumentacion()).orElse("");
        }
        return "";
    }

    private String obtenerIdPeticion(Peticion pet) {
        return (pet != null && pet.getAtributos() != null)
                ? pet.getAtributos().getIdPeticion()
                : null;
    }

    private SolicitudTransmision obtenerPrimeraSolicitud(Peticion pet) {
        if (pet == null || pet.getSolicitudes() == null
                || pet.getSolicitudes().getSolicitudTransmision() == null
                || pet.getSolicitudes().getSolicitudTransmision().isEmpty()) {
            return null;
        }
        return pet.getSolicitudes().getSolicitudTransmision().get(0);
    }

    private void guardarAsyncPeticion(Peticion pet, Respuesta resp,
                                      EstadoPeticionAsincrona estado, Integer ter) {
        String id = obtenerIdPeticion(pet);
        AsyncPeticion ap = asyncPeticionRepository.findByIdPeticion(id);
        if (ap == null) {
            ap = new AsyncPeticion();
            ap.setIdPeticion(id);
        }
        ap.setXmlPeticion(XmlUtil.toXml(pet));
        ap.setEstado(estado.getCodigo());
        if (ter != null) ap.setTer(ter);
        if (resp != null) ap.setXmlRespuesta(XmlUtil.toXml(resp));
        asyncPeticionRepository.save(ap);
    }

    private Respuesta reconstruirRespuesta(String xml, String id) {
        if (xml == null || xml.isBlank()) return null;
        try {
            return XmlUtil.fromXml(xml, Respuesta.class);
        } catch (Exception e) {
            log.error("No ha sido posible reconstruir la respuesta {}", id, e);
            return null;
        }
    }

    private Peticion reconstruirPeticion(AsyncPeticion async, Audit audit, String id) {
        String xml = (async != null) ? async.getXmlPeticion() : null;
        if ((xml == null || xml.isBlank()) && audit != null) {
            xml = audit.getXmlPeticion();
        }
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("No existe petición almacenada para " + id);
        }
        return XmlUtil.fromXml(xml, Peticion.class);
    }

    // Optionally include your scheduler here
    @Transactional
    @Scheduled(fixedDelayString = "${ittxartela.async.polling-interval-ms:60000}")
    public void procesarPeticionesAsincronasPendientes() {
        // your logic
    }
}
