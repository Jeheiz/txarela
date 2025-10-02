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
import es.map.xml_schemas.IntermediacionOnlineAsyncPortType;
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

    private static final QName SYNCSERVICENAME = new QName("http://www.map.es/xml-schemas",
            "x53jiServicioOnlineIntermediacion");
private static final QName SYNCPORTNAME = new QName("http://www.map.es/xml-schemas", "IntermediacionOnlinePort");
private static final QName ASYNCSERVICENAME = new QName("http://www.map.es/xml-schemas",
            "x53jiServicioOnlineIntermediacionAsync");
private static final QName ASYNCPORTNAME = new QName("http://www.map.es/xml-schemas", "IntermediacionOnlineAsyncPort");
	@Autowired
	AuditInputInterceptor auditInputInterceptor;


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
                        es.redsara.intermediacion.scsp.esquemas.datosespecificos.DatosEspecificosItTxartela.class
                    }
                );
*/
        endpoint.setServiceName(SYNCSERVICENAME);
        endpoint.setEndpointName(SYNCPORTNAME);
        /* WS-Security */
        Map<String, Object> inProps = new HashMap<>();

        inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP);
        inProps.put(WSHandlerConstants.SIG_VER_PROP_FILE, "server-crypto.properties");
        inProps.put(WSHandlerConstants.TTL_TIMESTAMP, "300");

        //WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps);
        //endpoint.getInInterceptors().add(wssIn);//
        /**/

        /* Validador *
        endpoint.getInInterceptors().add(onlineRequestValidator);/

        /* Audit Interceptors */
        //Esta línea añade un interceptor de validación personalizado (OnlineRequestValidator) al pipeline de entrada del servicio web.
        // Esto se ejecuta antes de que se procese la solicitud, 
        //
        //endpoint.getInInterceptors().add(auditInputInterceptor);
        //endpoint.getOutInterceptors().add(auditOutputInterceptor);
        /**/
        return endpoint;
    }


        @Bean
        Endpoint itTxartelaOnlineAsyncServiceEndpoint(Bus bus, IntermediacionOnlineAsyncPortType service) {
            log.info("Se procede a crear el servicio async");
            EndpointImpl endpoint = new EndpointImpl(bus, service);
            endpoint.publish("/ittxartela/online/async");
            endpoint.setWsdlLocation("classpath:/wsdl/online/x53jiServicioIntermediacion.wsdl");
            endpoint.setServiceName(ASYNCSERVICENAME);
            endpoint.setEndpointName(ASYNCPORTNAME);

            Map<String, Object> inProps = new HashMap<>();
            inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP);
            inProps.put(WSHandlerConstants.SIG_VER_PROP_FILE, "server-crypto.properties");
            inProps.put(WSHandlerConstants.TTL_TIMESTAMP, "300");

            WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps);
            endpoint.getInInterceptors().add(wssIn);
            endpoint.getInInterceptors().add(onlineRequestValidator);

            return endpoint;
        }

    }