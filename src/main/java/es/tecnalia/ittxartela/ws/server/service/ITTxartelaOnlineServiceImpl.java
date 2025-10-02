package es.tecnalia.ittxartela.ws.server.service;

import java.sql.Date;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Atributos;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Estado;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Respuesta;
import es.tecnalia.ittxartela.ws.server.constant.EstadoEspecifico;
import es.tecnalia.ittxartela.ws.server.constant.EstadoPeticionAsincrona;
import es.tecnalia.ittxartela.ws.server.model.Audit;
import es.tecnalia.ittxartela.ws.server.model.Matricula;
import es.tecnalia.ittxartela.ws.server.repository.AuditRepository;
import es.tecnalia.ittxartela.ws.server.repository.MatriculaRepository;
import es.tecnalia.ittxartela.ws.server.util.PeticionUtils;
import es.tecnalia.ittxartela.ws.server.util.PeticionValidator;
import es.tecnalia.ittxartela.ws.server.util.Utils;
import es.tecnalia.ittxartela.ws.server.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ITTxartelaOnlineServiceImpl implements IntermediacionOnlinePortType,
                IntermediacionOnlineAsyncPortType {

        private static final Duration TIEMPO_RESPUESTA_POR_DEFECTO = Duration.ofDays(5);
        private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        @Autowired
        private ModelMapper mapper;

        @Autowired
        private PeticionValidator peticionValidator;

        @Autowired
        private MatriculaRepository matriculaRepository;

        @Autowired
        private AuditRepository auditRepository;

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

                        audit.setMensajeEstado(String.format("Petición rechazada: %s", error.get().getMensaje()));
                        audit.setDetalleError(error.get().getMensaje());
                        audit.setXmlRespuesta(XmlUtil.toXml(respuestaError));
                        audit.setFechaDisponible(LocalDateTime.now());
                        auditRepository.save(audit);

                        log.info("peticionAsincrona rechazada {}", XmlUtil.toXml(respuestaError));

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

                log.info("peticionAsincrona acuse generado {}", XmlUtil.toXml(respuesta));

                return respuesta;
        }

        @Transactional
        public void procesarPeticionesAsincronasPendientes() {

                List<Audit> pendientes = auditRepository.findByEstadoOrderByFechaCreacionAsc(EstadoPeticionAsincrona.RECIBIDA.getCodigo());

                for (Audit audit : pendientes) {
                        try {
                                Peticion peticion = XmlUtil.fromXml(audit.getXmlPeticion(), Peticion.class);
                                Respuesta respuestaFinal = generarRespuestaValidada(peticion);

                                audit.setXmlRespuesta(XmlUtil.toXml(respuestaFinal));
                                audit.setEstado(EstadoPeticionAsincrona.DISPONIBLE.getCodigo());
                                audit.setMensajeEstado(EstadoPeticionAsincrona.DISPONIBLE.getDescripcion());
                                audit.setDetalleError(null);
                                audit.setFechaDisponible(LocalDateTime.now());
                                auditRepository.save(audit);

                                log.info("Petición asincrona {} procesada correctamente", audit.getIdPeticion());
                        } catch (Exception e) {
                                log.error("Error procesando la petición asincrona {}", audit.getIdPeticion(), e);

                                audit.setEstado(EstadoPeticionAsincrona.ERROR.getCodigo());
                                audit.setMensajeEstado(EstadoPeticionAsincrona.ERROR.getDescripcion());
                                audit.setDetalleError(e.getMessage());
                                audit.setFechaDisponible(LocalDateTime.now());
                                auditRepository.save(audit);
                        }
                }
        }

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

                Audit audit = auditRepository.findTopByIdPeticionOrderByIdDesc(idPeticion);

                if (audit == null) {
                        throw new IllegalArgumentException("No se ha encontrado la petición " + idPeticion);
                }

                if (EstadoPeticionAsincrona.DISPONIBLE.matches(audit.getEstado()) && audit.getXmlRespuesta() != null) {
                        try {
                                return XmlUtil.fromXml(audit.getXmlRespuesta(), Respuesta.class);
                        } catch (IllegalArgumentException e) {
                                log.error("No ha sido posible reconstruir la respuesta almacenada para {}", idPeticion, e);
                        }
                }

                Peticion peticion = XmlUtil.fromXml(audit.getXmlPeticion(), Peticion.class);
                Respuesta respuesta = construirRespuestaBase(peticion);
                Estado estado = asegurarEstado(respuesta);
                estado.setCodigoEstado(audit.getEstado());
                estado.setLiteralError(audit.getMensajeEstado());
                if (EstadoPeticionAsincrona.RECIBIDA.matches(audit.getEstado())) {
                        establecerTiempoEstimadoRespuesta(estado,
                                        Duration.between(LocalDateTime.now(), audit.getFechaRespuestaPrevista()));
                } else {
                        estado.setTiempoEstimadoRespuesta(0);
                }

                asegurarTransmisionDatos(respuesta).setDatosEspecificos(crearDatosEstado(peticion, audit));

                audit.setXmlRespuesta(XmlUtil.toXml(respuesta));
                auditRepository.save(audit);

                return respuesta;
        }

        private Respuesta generarRespuestaValidada(Peticion peticion) {

                Respuesta respuesta = construirRespuestaBase(peticion);
                Optional<PeticionValidator.RespuestaError> error = peticionValidator.validar(peticion);

                if (error.isPresent()) {
                        aplicarError(respuesta, error.get());
                } else {
                        aplicarRespuestaCorrecta(peticion, respuesta);
                }

                return respuesta;
        }

        private Respuesta construirRespuestaBase(Peticion peticion) {

                Respuesta respuesta = new Respuesta();
                mapper.map(peticion, respuesta);

                asegurarAtributos(respuesta).setTimeStamp(XmlUtil.obtenerFechaHoraXml());
                asegurarEstado(respuesta);

                es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.TransmisionDatos transmision = asegurarTransmisionDatos(respuesta);

                if (transmision.getDatosGenericos() == null) {
                        transmision.setDatosGenericos(new es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.DatosGenericos());
                }

                if (transmision.getDatosGenericos().getTransmision() == null) {
                        transmision.getDatosGenericos().setTransmision(new es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Transmision());
                }

                transmision.getDatosGenericos().getTransmision().setFechaGeneracion(XmlUtil.obtenerFechaHoraXml());

                return respuesta;
        }

        private es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.TransmisionDatos asegurarTransmisionDatos(Respuesta respuesta) {

                if (respuesta.getTransmisiones() == null) {
                        respuesta.setTransmisiones(new es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Transmisiones());
                }

                List<es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.TransmisionDatos> transmisiones =
                                respuesta.getTransmisiones().getTransmisionDatos();

                if (transmisiones.isEmpty()) {
                        transmisiones.add(new es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.TransmisionDatos());
                }

                return transmisiones.get(0);
        }

        private void aplicarError(Respuesta respuesta, PeticionValidator.RespuestaError error) {

                Estado estado = asegurarEstado(respuesta);
                estado.setCodigoEstado(error.getCodigo());
                estado.setLiteralError(error.getMensaje());
                estado.setTiempoEstimadoRespuesta(0);
        }

        private void aplicarRespuestaCorrecta(Peticion peticion, Respuesta respuesta) {

                Estado estado = asegurarEstado(respuesta);
                estado.setCodigoEstado("0003");
                estado.setLiteralError("Tramitada correctamente");
                estado.setTiempoEstimadoRespuesta(0);

                asegurarTransmisionDatos(respuesta).setDatosEspecificos(createDatosEspecificosRespuesta(peticion));
        }

        private DatosEspecificosItTxartela crearDatosEstado(Peticion peticion, Audit audit) {

                DatosEspecificosItTxartela datosEspecificos = new DatosEspecificosItTxartela();

                Retorno retorno = new Retorno();
                Traza traza = new Traza();

                DatosEspecificosItTxartela datosEspecificosPeticion = obtenerDatosEspecificosPeticion(peticion);
                Consulta consulta = datosEspecificosPeticion != null ? datosEspecificosPeticion.getConsulta() : null;

                if (consulta != null) {
                        traza.setIdTraza(consulta.getIdTraza());
                        datosEspecificos.setConsulta(consulta);
                }

                traza.setIdPeticion(peticion.getAtributos().getIdPeticion());

                retorno.setDatosTraza(traza);

                EstadoResultado estadoResultado = new EstadoResultado();
                estadoResultado.setResultado(audit.getEstado());
                estadoResultado.setDescripcion(construirDescripcionEstado(audit));

                retorno.setEstadoResultado(estadoResultado);

                datosEspecificos.setRetorno(retorno);

                return datosEspecificos;
        }

        private String construirDescripcionEstado(Audit audit) {

                StringBuilder descripcion = new StringBuilder(audit.getMensajeEstado());

                if (audit.getFechaRespuestaPrevista() != null && EstadoPeticionAsincrona.RECIBIDA.matches(audit.getEstado())) {
                        descripcion.append(". Tiempo estimado de respuesta: ")
                                        .append(audit.getFechaRespuestaPrevista().format(FORMATO_FECHA_HORA));
                }

                if (audit.getFechaDisponible() != null && EstadoPeticionAsincrona.DISPONIBLE.matches(audit.getEstado())) {
                        descripcion.append(". Disponible desde: ")
                                        .append(audit.getFechaDisponible().format(FORMATO_FECHA_HORA));
                }

                if (audit.getDetalleError() != null && !audit.getDetalleError().isBlank()) {
                        descripcion.append(". Detalle: ").append(audit.getDetalleError());
                }

                return descripcion.toString();
        }

        private Atributos asegurarAtributos(Respuesta respuesta) {

                if (respuesta.getAtributos() == null) {
                        respuesta.setAtributos(new Atributos());
                }

                return respuesta.getAtributos();
        }

        private Estado asegurarEstado(Respuesta respuesta) {

                Atributos atributos = asegurarAtributos(respuesta);

                if (atributos.getEstado() == null) {
                        atributos.setEstado(new Estado());
                }

                return atributos.getEstado();
        }

        private void establecerTiempoEstimadoRespuesta(Estado estado, Duration duracion) {

                if (duracion == null) {
                        estado.setTiempoEstimadoRespuesta(null);
                        return;
                }

                Duration restante = duracion.isNegative() ? Duration.ZERO : duracion;
                long dias = restante.toDays();
                int valor = dias > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) dias;
                estado.setTiempoEstimadoRespuesta(valor);
        }

        private DatosEspecificosItTxartela obtenerDatosEspecificosPeticion(Peticion peticion) {

                if (peticion.getSolicitudes() != null
                                && peticion.getSolicitudes().getSolicitudTransmision() != null
                                && !peticion.getSolicitudes().getSolicitudTransmision().isEmpty()) {
                        return peticion.getSolicitudes().getSolicitudTransmision().get(0).getDatosEspecificos();
                }

                return null;
        }

        private Audit crearRegistroAuditoria(Peticion peticion, EstadoPeticionAsincrona estado, LocalDateTime fechaPrevista) {

                Audit audit = new Audit();
                audit.setIdPeticion(peticion.getAtributos().getIdPeticion());
                audit.setXmlPeticion(XmlUtil.toXml(peticion));
                audit.setEstado(estado.getCodigo());
                audit.setMensajeEstado(estado.getDescripcion());
                audit.setDetalleError(null);
                LocalDateTime fechaEstimadaRespuesta = fechaPrevista != null
                                ? fechaPrevista
                                : LocalDateTime.now().plus(TIEMPO_RESPUESTA_POR_DEFECTO);
                audit.setFechaRespuestaPrevista(fechaEstimadaRespuesta);
                audit.setFechaDisponible(null);

                return auditRepository.save(audit);
        }

        private DatosEspecificosItTxartela createDatosEspecificosRespuesta(Peticion peticion) {

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

                        if (matriculas.size() > 0) {

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

        public Personas extraerDatosPersonas(List<Matricula> matriculas) {

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

        private Optional<EstadoResultado> validarDatosEspecificos(
                        DatosEspecificosItTxartela datosEspecificosPeticion) {

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