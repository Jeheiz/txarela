package es.tecnalia.ittxartela.ws.server.service;

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

	@Override
	public Respuesta peticionSincrona(Peticion peticion) {

		log.info("peticionSincrona recibida {}", XmlUtil.toXml(peticion));

		Respuesta respuesta = new Respuesta();

		mapper.map(peticion, respuesta);


		// Validación de la petición
		Optional<PeticionValidator.RespuestaError> error = peticionValidator.validar(peticion);

		if (error.isPresent()) {

			respuesta.getAtributos().getEstado().setCodigoEstado(error.get().getCodigo());
			respuesta.getAtributos().getEstado().setLiteralError(error.get().getMensaje());
			respuesta.getAtributos().setTimeStamp(XmlUtil.obtenerFechaHoraXml());
			respuesta.getTransmisiones().getTransmisionDatos().get(0).getDatosGenericos().getTransmision().setFechaGeneracion(XmlUtil.obtenerFechaHoraXml());
		} else {

			respuesta.getAtributos().getEstado().setCodigoEstado("0003");
			respuesta.getAtributos().getEstado().setLiteralError("Tramitada correctamente");
			respuesta.getAtributos().setTimeStamp(XmlUtil.obtenerFechaHoraXml());
			respuesta.getTransmisiones().getTransmisionDatos().get(0).getDatosGenericos().getTransmision().setFechaGeneracion(XmlUtil.obtenerFechaHoraXml());

			DatosEspecificosOnlineRespuestaItTxartela datosEspecificos = createDatosEspecificosRespuesta(peticion);

			respuesta.getTransmisiones().getTransmisionDatos().get(0).setDatosEspecificos(datosEspecificos);

		}

		log.info("peticionSincrona respuesta generada {}", XmlUtil.toXml(respuesta));

		return respuesta;
	}

	private DatosEspecificosOnlineRespuestaItTxartela createDatosEspecificosRespuesta(Peticion peticion) {

		log.info("Identificadores de documento solicitados {}", PeticionUtils.extraerDocumentacionTitulares(peticion));
		log.info("Titulares sin documento solicitados {}", PeticionUtils.extraerTitularesSinDocumentacion(peticion));

		DatosEspecificosOnlineRespuestaItTxartela datosEspecificos = new DatosEspecificosOnlineRespuestaItTxartela();

		DatosEspecificosOnlinePeticionItTxartela datosEspecificosPeticion = peticion.getSolicitudes().getSolicitudTransmision().get(0).getDatosEspecificos();


		EstadoResultado estado = new EstadoResultado();
		Personas personas = null;

		estado.setResultado(EstadoEspecifico.DATA_NOT_FOUND.getResultado());
		estado.setDescripcion(EstadoEspecifico.DATA_NOT_FOUND.getDescripcion());

		Optional<EstadoResultado> estadoValidacion = validarDatosEspecificos(datosEspecificosPeticion);

		if (estadoValidacion.isEmpty()) {

			java.sql.Date fechaLimite = null;

			try {

				fechaLimite = PeticionUtils.extraerFechaLimite(datosEspecificosPeticion);
			} catch (ParseException e) {
				//Nunca debe darse porque la fecha esta previamente validada
				e.printStackTrace();
			}
				
			List<Matricula> matriculas = obtenerExamenesRealizadosYSuperados(
						datosEspecificosPeticion.getDocumentacion(), 
						datosEspecificosPeticion.getTipoCertificacion(),
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

		traza.setIdTraza(datosEspecificosPeticion.getIdTraza());
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
			DatosEspecificosOnlinePeticionItTxartela datosEspecificosPeticion) {

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

		return matriculaRepository.findExamenesRealizadosYSuperados(dni, plataforma, fecha);
	}

}
