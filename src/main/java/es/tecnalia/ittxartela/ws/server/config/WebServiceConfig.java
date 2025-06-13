package es.tecnalia.ittxartela.ws.server.config;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.map.xml_schemas.IntermediacionOnlinePortType;
import es.tecnalia.ittxartela.ws.server.interceptor.AuditInputInterceptor;
import es.tecnalia.ittxartela.ws.server.interceptor.AuditOutputInterceptor;
import es.tecnalia.ittxartela.ws.server.interceptor.OnlineRequestValidator;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuración de Apache CXF para exponer servicios JAX-WS.
 */
@Slf4j
@Configuration
public class WebServiceConfig {

	 private static final QName SYNCSERVICENAME = new QName("http://www.map.es/xml-schemas", "x53JiServicioOnlineIntermediacion");

	@Autowired
	AuditInputInterceptor auditInputInterceptor;

	@Autowired
	AuditOutputInterceptor auditOutputInterceptor;

	@Autowired
	OnlineRequestValidator onlineRequestValidator;

	@Bean(name = Bus.DEFAULT_BUS_ID)
	SpringBus springBus() {
		return new SpringBus();
	}

    @Bean
    Endpoint itTxartelaOnlineServiceEndpoint(Bus bus, IntermediacionOnlinePortType service) {
    	log.info("Se procede a crear el servicio sync");
        EndpointImpl endpoint = new EndpointImpl(bus, service);
        endpoint.publish("/ittxartela/online");
        endpoint.setWsdlLocation("classpath:/wsdl/online/x53jiServicioIntermediacion.wsdl");
/*
        endpoint.setProperties(Map.of(
        	"schema-validation-enabled", true,
        	"schema-validation-enabled-in" , true,
        	"schema-validation-enabled-out", true
        ));

        endpoint.getProperties().put(
        	    "jaxb.additionalContextClasses",
        	    new Class[] {
        	        es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.peticion.datosespecificos.DatosEspecificosOnlinePeticionItTxartela.class
        	    }
        	);
*/
        endpoint.setServiceName(SYNCSERVICENAME);

        /* WS-Security */
        Map<String, Object> inProps = new HashMap<>();

        inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP);
        inProps.put(WSHandlerConstants.SIG_VER_PROP_FILE, "server-crypto.properties");
        inProps.put(WSHandlerConstants.TTL_TIMESTAMP, "300");

        WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps);
        endpoint.getInInterceptors().add(wssIn);
        /**/

        /* Validador */
        endpoint.getInInterceptors().add(onlineRequestValidator);

        /* Audit Interceptors */
        //no me gusta cómo solución para la auditoría. Se ejecuta antes de validarse seguridad o datos.
        //mejor gestionarlo en cada servicio, eso permite controlar datos que puedan querese guardar en campos concretos
        //y tener control de la entidad creada, lo que permitiría guardar petición y respuesta en el mismo registro.
        //endpoint.getInInterceptors().add(auditInputInterceptor);
        //endpoint.getOutInterceptors().add(auditOutputInterceptor);
        /**/
        return endpoint;
    }

}
