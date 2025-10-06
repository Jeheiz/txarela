package es.tecnalia.ittxartela.ws.server.service;

import java.sql.Date;
import java.text.ParseException;
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
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Certificado;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Certificados;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Consulta;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.DatosEspecificosItTxartela;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.EstadoResultado;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Persona;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Personas;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Retorno;
import es.redsara.intermediacion.scsp.esquemas.datosespecificos.Traza;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.Peticion;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.SolicitudTransmision;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Atributos;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Estado;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.DatosGenericos;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Respuesta;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.TransmisionDatos;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Transmision;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Transmisiones;
import es.tecnalia.ittxartela.ws.server.constant.EstadoEspecifico;
import es.tecnalia.ittxartela.ws.server.constant.EstadoPeticionAsincrona;
import es.tecnalia.ittxartela.ws.server.model.Audit;
import es.tecnalia.ittxartela.ws.server.model.AsyncPeticion;
import es.tecnalia.ittxartela.ws.server.model.Matricula;
import es.tecnalia.ittxartela.ws.server.repository.AuditRepository;
import es.tecnalia.ittxartela.ws.server.repository.AsyncPeticionRepository;
import es.tecnalia.ittxartela.ws.server.repository.MatriculaRepository;
import es.tecnalia.ittxartela.ws.server.util.PeticionValidator;
import es.tecnalia.ittxartela.ws.server.util.PeticionUtils;
import es.tecnalia.ittxartela.ws.server.util.Utils;
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

    @Autowired
    private MatriculaRepository matriculaRepository;

    // --- Síncrono ---
    @Override
    @Transactional
    public Respuesta peticionSincrona(Peticion peticion) {
        log.info("peticionSincrona recibida {}", XmlUtil.toXml(peticion));

        Audit audit = crearRegistroAuditoria(peticion, EstadoPeticionAsincrona.DISPONIBLE, LocalDateTime.now());

        Respuesta respuesta = generarRespuestaValidada(peticion);

        audit.setXmlRespuesta(XmlUtil.toXml(respuesta));
   

        Estado estadoRespuesta = respuesta != null && respuesta.getAtributos() != null
                ? respuesta.getAtributos().getEstado()
                : null;

        LocalDateTime ahora = LocalDateTime.now();

        if (estadoRespuesta != null
                && EstadoPeticionAsincrona.ERROR.getCodigo().equals(estadoRespuesta.getCodigoEstado())) {
            audit.setEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
            audit.setMensajeEstado(EstadoPeticionAsincrona.ERROR.getDescripcion());
            audit.setDetalleError(estadoRespuesta.getLiteralError());
            audit.setFechaDisponible(ahora);
        } else {
            audit.setEstado(EstadoPeticionAsincrona.DISPONIBLE.getCodigo());
            audit.setMensajeEstado(EstadoPeticionAsincrona.DISPONIBLE.getDescripcion());
            audit.setDetalleError(null);
            audit.setFechaDisponible(ahora);
        }


        auditRepository.save(audit);

        log.info("peticionSincrona respuesta generada {}", XmlUtil.toXml(respuesta));

        return respuesta;
    }


    @Override
    @Transactional
    public Respuesta peticionAsincrona(Peticion peticion) {
        log.info("peticionAsincrona recibida {}", XmlUtil.toXml(peticion));

        Optional<PeticionValidator.RespuestaError> error = peticionValidator.validar(peticion);

        if (error.isPresent()) {
            Audit audit = crearRegistroAuditoria(peticion, EstadoPeticionAsincrona.ERROR, LocalDateTime.now());

            Respuesta respuestaError = construirRespuestaBase(peticion);
            aplicarError(respuestaError, peticion, error.get());
         
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

@@ -190,72 +227,477 @@ public class ITTxartelaOnlineServiceImpl implements IntermediacionOnlinePortType

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
    private Respuesta generarRespuestaValidada(Peticion peticion) {
        Objects.requireNonNull(peticion, "La petición no puede ser nula");

        Respuesta respuesta = construirRespuestaBase(peticion);

        Optional<PeticionValidator.RespuestaError> error = peticionValidator.validar(peticion);
        if (error.isPresent()) {
            aplicarError(respuesta, peticion, error.get());
        } else {
            aplicarRespuestaCorrecta(respuesta, peticion);
        }

        return respuesta;
    }

    private Respuesta construirRespuestaBase(Peticion peticion) {
        Respuesta respuesta = new Respuesta();
        mapper.map(peticion, respuesta);

        if (respuesta.getAtributos() == null) {
            respuesta.setAtributos(new Atributos());
        }

        respuesta.getAtributos().setTimeStamp(XmlUtil.obtenerFechaHoraXml());
        asegurarEstado(respuesta);

        TransmisionDatos transmision = asegurarTransmisionDatos(respuesta);
        if (transmision.getDatosGenericos() == null) {
            transmision.setDatosGenericos(new DatosGenericos());
        }
        if (transmision.getDatosGenericos().getTransmision() == null) {
            transmision.getDatosGenericos().setTransmision(new Transmision());
        }
        transmision.getDatosGenericos().getTransmision().setFechaGeneracion(XmlUtil.obtenerFechaHoraXml());

        return respuesta;
    }

    private Audit crearRegistroAuditoria(Peticion peticion, EstadoPeticionAsincrona estadoInicial,
                                         LocalDateTime fechaRespuestaPrevista) {
        Objects.requireNonNull(peticion, "La petición no puede ser nula");
        Objects.requireNonNull(estadoInicial, "El estado inicial es obligatorio");

        Audit audit = new Audit();
        audit.setIdPeticion(obtenerIdPeticion(peticion));
        audit.setXmlPeticion(XmlUtil.toXml(peticion));
        audit.setEstado(estadoInicial.getCodigo());
        audit.setMensajeEstado(estadoInicial.getDescripcion());
        audit.setDetalleError(null);
        audit.setFechaRespuestaPrevista(
                fechaRespuestaPrevista != null ? fechaRespuestaPrevista : LocalDateTime.now().plus(TIEMPO_RESPUESTA_POR_DEFECTO));
        audit.setFechaDisponible(null);

        return audit;
    }

    private void aplicarError(Respuesta respuesta, Peticion peticion, PeticionValidator.RespuestaError error) {
        Estado estado = asegurarEstado(respuesta);
        estado.setCodigoEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
        estado.setCodigoEstadoSecundario(error.getCodigo());
        estado.setLiteralError(error.getMensaje());
        estado.setTiempoEstimadoRespuesta(0);

        asegurarTransmisionDatos(respuesta).setDatosEspecificos(crearDatosEspecificosError(peticion, error));
    }

    private void aplicarRespuestaCorrecta(Respuesta respuesta, Peticion peticion) {
        Estado estado = asegurarEstado(respuesta);
        estado.setCodigoEstado("0003");
        estado.setCodigoEstadoSecundario(null);
        estado.setLiteralError("Tramitada correctamente");
        estado.setTiempoEstimadoRespuesta(0);

        asegurarTransmisionDatos(respuesta).setDatosEspecificos(crearDatosEspecificosRespuesta(peticion));
    }

    private Estado asegurarEstado(Respuesta respuesta) {
        if (respuesta.getAtributos().getEstado() == null) {
            respuesta.getAtributos().setEstado(new Estado());
        }
        return respuesta.getAtributos().getEstado();
    }

    private void establecerTiempoEstimadoRespuesta(Estado estado, Duration duracion) {
        if (estado == null || duracion == null) {
            return;
        }

        long minutos = Math.max(0, duracion.toMinutes());
        estado.setTiempoEstimadoRespuesta((int) Math.min(Integer.MAX_VALUE, minutos));
    }

    private TransmisionDatos asegurarTransmisionDatos(Respuesta respuesta) {
        if (respuesta.getTransmisiones() == null) {
            respuesta.setTransmisiones(new Transmisiones());
        }

        List<TransmisionDatos> transmisiones = respuesta.getTransmisiones().getTransmisionDatos();
        if (transmisiones.isEmpty()) {
            transmisiones.add(new TransmisionDatos());
        }

        TransmisionDatos transmision = transmisiones.get(0);
        if (transmision.getDatosGenericos() == null) {
            transmision.setDatosGenericos(new DatosGenericos());
        }
        if (transmision.getDatosGenericos().getTransmision() == null) {
            transmision.getDatosGenericos().setTransmision(new Transmision());
        }

        return transmision;
    }

    private DatosEspecificosItTxartela crearDatosEstado(Peticion peticion, Audit audit) {
        LocalDateTime fechaReferencia = audit != null && audit.getFechaRespuestaPrevista() != null
                ? audit.getFechaRespuestaPrevista()
                : LocalDateTime.now().plus(TIEMPO_RESPUESTA_POR_DEFECTO);

        Consulta consulta = copiarConsultaDesdePeticion(peticion);
        if (consulta == null) {
            consulta = construirConsultaPorDefecto(peticion, fechaReferencia);
        } else {
            if (consulta.getFechaLimite() == null || consulta.getFechaLimite().isBlank()) {
                consulta.setFechaLimite(fechaReferencia.format(FORMATO_FECHA_HORA));
            }
            if (consulta.getDocumentacion() == null || consulta.getDocumentacion().isBlank()) {
                consulta.setDocumentacion(obtenerDocumentoPrincipal(peticion));
            }
            if (consulta.getIdTraza() == null || consulta.getIdTraza().isBlank()) {
                consulta.setIdTraza(obtenerIdPeticion(peticion));
            }
        }

        DatosEspecificosItTxartela datos = new DatosEspecificosItTxartela();
        datos.setConsulta(consulta);

        Retorno retorno = new Retorno();
        Traza traza = new Traza();
        traza.setIdTraza(consulta.getIdTraza());
        traza.setIdPeticion(obtenerIdPeticion(peticion));
        retorno.setDatosTraza(traza);

        EstadoResultado estadoResultado = new EstadoResultado();
        estadoResultado.setResultado(audit != null && audit.getEstado() != null
                ? audit.getEstado()
                : EstadoPeticionAsincrona.RECIBIDA.getCodigo());
        estadoResultado.setDescripcion(construirDescripcionEstado(audit));
        retorno.setEstadoResultado(estadoResultado);

        datos.setRetorno(retorno);

        return datos;
    }

    private DatosEspecificosItTxartela crearDatosEspecificosRespuesta(Peticion peticion) {
        log.info("Identificadores de documento solicitados {}", PeticionUtils.extraerDocumentacionTitulares(peticion));
        log.info("Titulares sin documento solicitados {}", PeticionUtils.extraerTitularesSinDocumentacion(peticion));

        DatosEspecificosItTxartela datosEspecificos = new DatosEspecificosItTxartela();

        DatosEspecificosItTxartela datosEspecificosPeticion = obtenerDatosEspecificosPeticion(peticion);
        Consulta consulta = datosEspecificosPeticion != null ? datosEspecificosPeticion.getConsulta() : null;

        EstadoResultado estado = new EstadoResultado();
        Personas personas = null;

        estado.setResultado(EstadoEspecifico.DATA_NOT_FOUND.getResultado());
        estado.setDescripcion(EstadoEspecifico.DATA_NOT_FOUND.getDescripcion());

        Optional<EstadoResultado> estadoValidacion = validarDatosEspecificos(datosEspecificosPeticion);

        if (estadoValidacion.isEmpty()) {
            Date fechaLimite = null;

            try {
                fechaLimite = PeticionUtils.extraerFechaLimite(consulta);
            } catch (ParseException e) {
                log.error("Error al parsear la fecha límite", e);
            }

            List<Matricula> matriculas = obtenerExamenesRealizadosYSuperados(
                    consulta != null ? consulta.getDocumentacion() : null,
                    consulta != null ? consulta.getTipoCertificacion() : null,
                    fechaLimite
            );

            if (!matriculas.isEmpty()) {
                personas = extraerDatosPersonas(matriculas);
                estado.setResultado(EstadoEspecifico.OK.getResultado());
                estado.setDescripcion(EstadoEspecifico.OK.getDescripcion());
            }
        } else {
            estado = estadoValidacion.get();
        }

        Retorno retorno = new Retorno();

        Traza traza = new Traza();

        if (consulta != null) {
            traza.setIdTraza(consulta.getIdTraza());
            datosEspecificos.setConsulta(consulta);
        } else {
            Consulta consultaPorDefecto = construirConsultaPorDefecto(peticion, LocalDateTime.now());
            datosEspecificos.setConsulta(consultaPorDefecto);
            traza.setIdTraza(consultaPorDefecto.getIdTraza());
            consulta = consultaPorDefecto;
        }

        traza.setIdPeticion(peticion.getAtributos().getIdPeticion());

        retorno.setDatosTraza(traza);
        retorno.setEstadoResultado(estado);

        if (personas != null) {
            retorno.setPersonas(personas);
        }

        datosEspecificos.setRetorno(retorno);

        return datosEspecificos;
    }

    private DatosEspecificosItTxartela crearDatosEspecificosError(Peticion peticion, PeticionValidator.RespuestaError error) {
        DatosEspecificosItTxartela datos = new DatosEspecificosItTxartela();

        Consulta consulta = copiarConsultaDesdePeticion(peticion);
        if (consulta == null) {
            consulta = construirConsultaPorDefecto(peticion, LocalDateTime.now());
        }

        datos.setConsulta(consulta);

        Retorno retorno = new Retorno();
        Traza traza = new Traza();
        traza.setIdTraza(consulta.getIdTraza());
        traza.setIdPeticion(obtenerIdPeticion(peticion));
        retorno.setDatosTraza(traza);

        EstadoResultado estadoResultado = new EstadoResultado();
        estadoResultado.setResultado(error.getCodigo());
        estadoResultado.setDescripcion(error.getMensaje());
        retorno.setEstadoResultado(estadoResultado);

        datos.setRetorno(retorno);

        return datos;
    }

    private Consulta copiarConsultaDesdePeticion(Peticion peticion) {
        SolicitudTransmision solicitud = obtenerPrimeraSolicitud(peticion);
        if (solicitud != null && solicitud.getDatosEspecificos() != null
                && solicitud.getDatosEspecificos().getConsulta() != null) {
            Consulta original = solicitud.getDatosEspecificos().getConsulta();
            Consulta copia = new Consulta();
            copia.setIdTraza(original.getIdTraza());
            copia.setDocumentacion(original.getDocumentacion());
            copia.setFechaLimite(original.getFechaLimite());
            copia.setTipoCertificacion(original.getTipoCertificacion());
            return copia;
        }
        return null;
    }

    private Consulta construirConsultaPorDefecto(Peticion peticion, LocalDateTime fechaPrevista) {
        Consulta consulta = new Consulta();
        consulta.setIdTraza(obtenerIdPeticion(peticion));
        consulta.setDocumentacion(obtenerDocumentoPrincipal(peticion));
        LocalDateTime fecha = fechaPrevista != null ? fechaPrevista : LocalDateTime.now().plus(TIEMPO_RESPUESTA_POR_DEFECTO);
        consulta.setFechaLimite(fecha.format(FORMATO_FECHA_HORA));
        consulta.setTipoCertificacion(0);
        return consulta;
    }

    private String construirDescripcionEstado(Audit audit) {
        if (audit == null) {
            return EstadoPeticionAsincrona.RECIBIDA.getDescripcion();
        }

        StringBuilder descripcion = new StringBuilder();
        if (audit.getMensajeEstado() != null && !audit.getMensajeEstado().isBlank()) {
            descripcion.append(audit.getMensajeEstado());
        }

        if (audit.getFechaRespuestaPrevista() != null && EstadoPeticionAsincrona.RECIBIDA.matches(audit.getEstado())) {
            if (descripcion.length() > 0) {
                descripcion.append(". ");
            }
            descripcion.append("Tiempo estimado de respuesta: ")
                    .append(audit.getFechaRespuestaPrevista().format(FORMATO_FECHA_HORA));
        }

        if (audit.getFechaDisponible() != null && EstadoPeticionAsincrona.DISPONIBLE.matches(audit.getEstado())) {
            if (descripcion.length() > 0) {
                descripcion.append(". ");
            }
            descripcion.append("Disponible desde: ")
                    .append(audit.getFechaDisponible().format(FORMATO_FECHA_HORA));
        }

        if (audit.getDetalleError() != null && !audit.getDetalleError().isBlank()) {
            if (descripcion.length() > 0) {
                descripcion.append(". ");
            }
            descripcion.append("Detalle: ").append(audit.getDetalleError());
        }

        return descripcion.toString();
    }

    private String obtenerDocumentoPrincipal(Peticion peticion) {
        SolicitudTransmision solicitud = obtenerPrimeraSolicitud(peticion);
        if (solicitud != null && solicitud.getDatosGenericos() != null
                && solicitud.getDatosGenericos().getTitular() != null) {
            return Optional.ofNullable(solicitud.getDatosGenericos().getTitular().getDocumentacion())
                    .orElse("");
        }
        return "";
    }

    private String obtenerIdPeticion(Peticion peticion) {
        return peticion != null && peticion.getAtributos() != null
                ? peticion.getAtributos().getIdPeticion()
                : null;
    }

    private SolicitudTransmision obtenerPrimeraSolicitud(Peticion peticion) {
        if (peticion == null || peticion.getSolicitudes() == null
                || peticion.getSolicitudes().getSolicitudTransmision() == null
                || peticion.getSolicitudes().getSolicitudTransmision().isEmpty()) {
            return null;
        }
        return peticion.getSolicitudes().getSolicitudTransmision().get(0);
    }

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

    
    private DatosEspecificosItTxartela obtenerDatosEspecificosPeticion(Peticion peticion) {
        SolicitudTransmision solicitud = obtenerPrimeraSolicitud(peticion);
        if (solicitud != null && solicitud.getDatosEspecificos() != null) {
            return solicitud.getDatosEspecificos();
        }
        return null;
    }

    private Personas extraerDatosPersonas(List<Matricula> matriculas) {
        Personas personas = new Personas();
        Persona personaActual = null;
        String dniAnterior = null;

        for (Matricula matricula : matriculas) {
            String dni = matricula.getDni();

            if (!dni.equals(dniAnterior)) {
                personaActual = new Persona();
                personaActual.setDocumentacion(dni);
                personaActual.setCertificados(new Certificados());
                personas.getPersona().add(personaActual);
                dniAnterior = dni;
            }

            Certificado certificado = new Certificado();
            certificado.setCodigoModulo(String.valueOf(matricula.getCod_modulo()));

            if (matricula.getFecha_exam() != null) {
                certificado.setFechaExamen(matricula.getFecha_exam().toString());
            }

            personaActual.getCertificados().getCertificado().add(certificado);
        }

        return personas;
    }

    private Optional<EstadoResultado> validarDatosEspecificos(DatosEspecificosItTxartela datosEspecificosPeticion) {
        Consulta consulta = datosEspecificosPeticion != null ? datosEspecificosPeticion.getConsulta() : null;

        if (consulta == null) {
            EstadoResultado estado = new EstadoResultado();
            estado.setResultado(EstadoEspecifico.DATA_NOT_FOUND.getResultado());
            estado.setDescripcion("No se han proporcionado datos específicos en la petición");
            return Optional.of(estado);
        }

        boolean esDniValido = Utils.esDNIValido(consulta.getDocumentacion());

        if (!esDniValido) {
            EstadoResultado estado = new EstadoResultado();
            estado.setResultado(EstadoEspecifico.NIF_INVALIDO.getResultado());
            estado.setDescripcion(EstadoEspecifico.NIF_INVALIDO.getDescripcion());
            return Optional.of(estado);
        }

        boolean esFechaValida = Utils.validaFecha(consulta.getFechaLimite());

        if (!esFechaValida) {
            EstadoResultado estado = new EstadoResultado();
            estado.setResultado(EstadoEspecifico.FECHA_INVALIDA.getResultado());
            estado.setDescripcion(EstadoEspecifico.FECHA_INVALIDA.getDescripcion());
            return Optional.of(estado);
        }

        return Optional.empty();
    }

    private List<Matricula> obtenerExamenesRealizadosYSuperados(String dni, Integer plataforma, Date fecha) {
        return matriculaRepository.findExamenesRealizadosYSuperados(dni, plataforma, fecha);
    }

}