ackage es.tecnalia.ittxartela.ws.server.service;

import java.sql.Date;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.map.xml_schemas.IntermediacionOnlinePortType;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.peticion.datosespecificos.DatosEspecificosOnlinePeticionItTxartela;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos.Certificado;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos.Certificados;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos.DatosEspecificosOnlineRespuestaItTxartela;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos.EstadoResultado;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos.Persona;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos.Personas;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos.Retorno;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos.Traza;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.Peticion;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Respuesta;
import es.tecnalia.ittxartela.ws.server.constant.EstadoEspecifico;
import es.tecnalia.ittxartela.ws.server.model.Matricula;
import es.tecnalia.ittxartela.ws.server.repository.MatriculaRepository;
import es.tecnalia.ittxartela.ws.server.constant.EstadoEspecifico;
import es.tecnalia.ittxartela.ws.server.model.Matricula;
import es.tecnalia.ittxartela.ws.server.model.Audit;
import es.tecnalia.ittxartela.ws.server.repository.MatriculaRepository;
import es.tecnalia.ittxartela.ws.server.repository.AuditRepository;
import es.tecnalia.ittxartela.ws.server.util.PeticionUtils;
import es.tecnalia.ittxartela.ws.server.util.PeticionValidator;
import es.tecnalia.ittxartela.ws.server.util.Utils;
import es.tecnalia.ittxartela.ws.server.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@WebService(
	    serviceName = "x53jiServicioOnlineIntermediacion",
	    portName = "IntermediacionOnlinePort",
	    targetNamespace = "http://www.map.es/xml-schemas",
	    endpointInterface = "es.map.xml_schemas.IntermediacionOnlinePortType"
	)
public class ITTxartelaOnlineServiceImpl implements IntermediacionOnlinePortType {

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private PeticionValidator peticionValidator;

	@Autowired
	private MatriculaRepository matriculaRepository;
	

    @Autowired
    private AuditRepository auditRepository;

    @Override
    public Respuesta peticionSincrona(Peticion peticion) {

            log.info("peticionSincrona recibida {}", XmlUtil.toXml(peticion));
            System.out.println("peticionSincrona recibida {}:" + XmlUtil.toXml(peticion));

            Respuesta respuesta = new Respuesta();
            mapper.map(peticion, respuesta);

            String idPeticion = peticion != null && peticion.getAtributos() != null
                            ? peticion.getAtributos().getIdPeticion()
                            : null;
            
            System.out.println(">>>GRABAMOS EN AUDIT LA PETICION:"+idPeticion);

            Audit audit = new Audit();
            audit.setIdPeticion(idPeticion);
            audit.setXmlPeticion(XmlUtil.toXml(peticion));
            audit = auditRepository.save(audit);

            Optional<PeticionValidator.RespuestaError> error;
            if (peticion == null || peticion.getAtributos() == null || idPeticion == null) {
            	    System.out.println("0401 - La estructura del fichero recibido no corresponde con el esquema");
                    error = Optional.of(new PeticionValidator.RespuestaError(
                                    "0401",
                                    "La estructura del fichero recibido no corresponde con el esquema."));
            } else {
            	    System.out.println("Llamada a peticionValidator con la petición");
                    error = peticionValidator.validar(peticion);
            }
            
            System.out.println(">>>ASIGNAMOS CIERTOS ATRIBUTOS A LA RESPUESTA");

            if (respuesta.getAtributos() == null) {
                    respuesta.setAtributos(new es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Atributos());
            }
            if (respuesta.getAtributos().getEstado() == null) {
                    respuesta.getAtributos().setEstado(new es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Estado());
            }

            if (error.isPresent()) {
            	
            	    System.out.println("Hay error presente");

                    respuesta.getAtributos().getEstado().setCodigoEstado(error.get().getCodigo());
                    respuesta.getAtributos().getEstado().setLiteralError(error.get().getMensaje());
                    respuesta.getAtributos().setTimeStamp(XmlUtil.obtenerFechaHoraXml());
                    if (respuesta.getTransmisiones() != null &&
                        respuesta.getTransmisiones().getTransmisionDatos() != null &&
                        !respuesta.getTransmisiones().getTransmisionDatos().isEmpty()) {
                            respuesta.getTransmisiones().getTransmisionDatos().get(0)
                                    .getDatosGenericos().getTransmision()
                                    .setFechaGeneracion(XmlUtil.obtenerFechaHoraXml());
                    }
            } else {
            	
            	   System.out.println("No hay error presente");

                    respuesta.getAtributos().getEstado().setCodigoEstado("0003");
                    respuesta.getAtributos().getEstado().setLiteralError("Tramitada correctamente");
                    respuesta.getAtributos().setTimeStamp(XmlUtil.obtenerFechaHoraXml());
                    if (respuesta.getTransmisiones() != null &&
                        respuesta.getTransmisiones().getTransmisionDatos() != null &&
                        !respuesta.getTransmisiones().getTransmisionDatos().isEmpty()) {
                            respuesta.getTransmisiones().getTransmisionDatos().get(0)
                                    .getDatosGenericos().getTransmision()
                                    .setFechaGeneracion(XmlUtil.obtenerFechaHoraXml());

                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                            System.out.println(">>>DatosEspecificosOnlineRespuestaItTxartela.createDatosEspecificosRespuesta<<<");
                            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                            DatosEspecificosOnlineRespuestaItTxartela datosEspecificos = createDatosEspecificosRespuesta(peticion);
                            respuesta.getTransmisiones().getTransmisionDatos().get(0)
                                    .setDatosEspecificos(datosEspecificos);
                    }

            }


           System.out.println(">>>GRABAMOS EN AUDIT LA RESPUESTA:"+XmlUtil.toXml(respuesta));
	       audit.setXmlRespuesta(XmlUtil.toXml(respuesta));
           auditRepository.save(audit);

           log.info("peticionSincrona respuesta generada {}", XmlUtil.toXml(respuesta));

           return respuesta;
	}

	private DatosEspecificosOnlineRespuestaItTxartela createDatosEspecificosRespuesta(Peticion peticion) {
		
		System.out.println("#####Entra en createDatosEspecificosRespuesta#####");

		log.info("Identificadores de documento solicitados {}", PeticionUtils.extraerDocumentacionTitulares(peticion));
		log.info("Titulares sin documento solicitados {}", PeticionUtils.extraerTitularesSinDocumentacion(peticion));

		DatosEspecificosOnlineRespuestaItTxartela datosEspecificos = new DatosEspecificosOnlineRespuestaItTxartela();

		DatosEspecificosOnlinePeticionItTxartela datosEspecificosPeticion = peticion.getSolicitudes().getSolicitudTransmision().get(0).getDatosEspecificos();


		EstadoResultado estado = new EstadoResultado();
		Personas personas = null;

		estado.setResultado(EstadoEspecifico.DATA_NOT_FOUND.getResultado());
		estado.setDescripcion(EstadoEspecifico.DATA_NOT_FOUND.getDescripcion());

		Optional<EstadoResultado> estadoValidacion = validarDatosEspecificos(datosEspecificosPeticion);
		
		System.out.println("Comprobamos estadoValidacion");

		if (estadoValidacion.isEmpty()) {
			
			System.out.println("estadoValidacion vacío");

			java.sql.Date fechaLimite = null;

			try {

				fechaLimite = PeticionUtils.extraerFechaLimite(datosEspecificosPeticion);
				System.out.println("fecha límite obtenida:"+fechaLimite);
			} catch (ParseException e) {
				//Nunca debe darse porque la fecha esta previamente validada
				e.printStackTrace();
			}
			
			System.out.println("Obtenemos las matrículas hechas y aprobadas para la fecha:"+fechaLimite+ "y la docu:"+datosEspecificosPeticion.getDocumentacion()+ " y el tipo de certificación:"+datosEspecificosPeticion.getTipoCertificacion());
				
			//Cambio para contemplar peticiones tipo AMBOS
			
			List<Matricula> matriculas = null;
			
			if (datosEspecificosPeticion.getTipoCertificacion()!=2) {
			
				matriculas = obtenerExamenesRealizadosYSuperados(
							datosEspecificosPeticion.getDocumentacion(), 
							datosEspecificosPeticion.getTipoCertificacion(),
							fechaLimite
							);
			
			}else {
				
				matriculas = obtenerExamenesRealizadosYSuperadosAmbos(
							datosEspecificosPeticion.getDocumentacion(),
							fechaLimite
							);
				
			}
			
			System.out.println("cantidad de certificaciones encontradas:"+matriculas.size());

			if (matriculas.size() > 0) {

				personas = extraerDatosPersonas(matriculas);
				estado.setResultado(EstadoEspecifico.OK.getResultado());
				estado.setDescripcion(EstadoEspecifico.OK.getDescripcion());
			}
		} else {
			
			System.out.println("estadoValidacion no vacío");

			estado = estadoValidacion.get();
		}
		
		System.out.println("Retorno y trazas");

		Retorno retorno = new Retorno();

		Traza traza = new Traza();

		traza.setIdTraza(datosEspecificosPeticion.getIdTraza());
		traza.setIdPeticion(peticion.getAtributos().getIdPeticion());

		retorno.setDatosTraza(traza);
		retorno.setEstadoResultado(estado);

		if (personas != null) {
			
			System.out.println("Personas NO null");
			retorno.setPersonas(personas);
			
		}else {
			
			System.out.println("Personas null");
			
		}

		datosEspecificos.setRetorno(retorno);

		return datosEspecificos;
	}

	public Personas extraerDatosPersonas(List<Matricula> matriculas) {
		
		System.out.println("Entra en extraerDatosPersonas");

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
			DatosEspecificosOnlinePeticionItTxartela datosEspecificosPeticion) {
		
		System.out.println("Entra en validarDatosEspecificos");

		boolean esDniValido = Utils.esDNIValido(datosEspecificosPeticion.getDocumentacion());

		if (!esDniValido) {

			EstadoResultado estado = new EstadoResultado();
			estado.setResultado(EstadoEspecifico.NIF_INVALIDO.getResultado());
			estado.setDescripcion(EstadoEspecifico.NIF_INVALIDO.getDescripcion());
			return Optional.of(estado);
		}

		boolean esFechaValida = Utils.validaFecha(datosEspecificosPeticion.getFechaLimite());

		if (!esFechaValida) {

			EstadoResultado estado = new EstadoResultado();
			estado.setResultado(EstadoEspecifico.FECHA_INVALIDA.getResultado());
			estado.setDescripcion(EstadoEspecifico.FECHA_INVALIDA.getDescripcion());
			return Optional.of(estado);
		}

		return Optional.empty();
	}

	private List<Matricula> obtenerExamenesRealizadosYSuperados(String dni, Integer plataforma, Date fecha) {
		
		System.out.println("IMPL: "+"dni:"+dni+" plataforma:"+plataforma+" fecha:"+fecha);

		return matriculaRepository.findExamenesRealizadosYSuperados(dni, plataforma, fecha);
	}
	
	//Para consultas donde nos pidan ambas certificaciones
	private List<Matricula> obtenerExamenesRealizadosYSuperadosAmbos(String dni, Date fecha) {
			
			System.out.println("IMPL: "+"dni:"+dni+" fecha:"+fecha);
	
			return matriculaRepository.findExamenesRealizadosYSuperadosAmbos(dni, fecha);
		}

}
