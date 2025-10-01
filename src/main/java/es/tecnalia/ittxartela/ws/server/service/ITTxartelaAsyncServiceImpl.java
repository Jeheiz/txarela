package es.tecnalia.ittxartela.ws.server.service;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.map.xml_schemas.IntermediacionOnlineAsyncPortType;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.Peticion;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Atributos;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Estado;
import es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta.Respuesta;
import es.tecnalia.ittxartela.ws.server.async.Acknowledge;
import es.tecnalia.ittxartela.ws.server.async.IdPeticion;
import es.tecnalia.ittxartela.ws.server.model.AsyncPeticion;
import es.tecnalia.ittxartela.ws.server.repository.AsyncPeticionRepository;
import es.tecnalia.ittxartela.ws.server.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;

@Slf4j
@Service
@WebService(
        serviceName = "x53jiServicioOnlineIntermediacionAsync",
        portName = "IntermediacionOnlineAsyncPort",
        targetNamespace = "http://www.map.es/xml-schemas",
        endpointInterface = "es.map.xml_schemas.IntermediacionOnlineAsyncPortType"
)
public class ITTxartelaAsyncServiceImpl implements IntermediacionOnlineAsyncPortType {

    @Autowired
    private ITTxartelaOnlineServiceImpl syncService;

    @Autowired
    private AsyncPeticionRepository repository;

    @Override
    public Acknowledge peticionAsincrona(Peticion peticion) {
        String id = peticion != null && peticion.getAtributos() != null
                ? peticion.getAtributos().getIdPeticion()
                : null;
        log.info("peticionAsincrona recibida {}", XmlUtil.toXml(peticion));

        Respuesta respuesta = syncService.peticionSincrona(peticion);

        AsyncPeticion entity = new AsyncPeticion();
        entity.setIdPeticion(id);
        entity.setXmlPeticion(XmlUtil.toXml(peticion));
        entity.setXmlRespuesta(XmlUtil.toXml(respuesta));
        entity.setEstado("0003");
        entity.setTer(5);
        repository.save(entity);

        Estado estado = new Estado();
        estado.setCodigoEstado("0002");
        estado.setLiteralError("EN PROCESO");
        estado.setTiempoEstimadoRespuesta(5);

        Acknowledge ack = new Acknowledge();
        ack.setIdPeticion(id);
        ack.setEstado(estado);
        return ack;
    }

    @Override
    public Respuesta solicitudRespuesta(IdPeticion solicitud) {
        String id = solicitud.getIdPeticion();
        AsyncPeticion entity = repository.findByIdPeticion(id);
        if (entity == null) {
            Respuesta resp = new Respuesta();
            Atributos attrs = new Atributos();
            Estado estado = new Estado();
            estado.setCodigoEstado("0002");
            estado.setLiteralError("EN PROCESO");
            attrs.setEstado(estado);
            resp.setAtributos(attrs);
            return resp;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(Respuesta.class);
            return (Respuesta) context.createUnmarshaller().unmarshal(new StringReader(entity.getXmlRespuesta()));
        } catch (JAXBException e) {
            log.error("Error deserializando respuesta", e);
            Respuesta resp = new Respuesta();
            Atributos attrs = new Atributos();
            Estado estado = new Estado();
            estado.setCodigoEstado("0002");
            estado.setLiteralError("EN PROCESO");
            attrs.setEstado(estado);
            resp.setAtributos(attrs);
            return resp;
        }
    }
}