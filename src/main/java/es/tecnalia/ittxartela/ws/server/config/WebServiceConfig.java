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
* Configuración centralizada de los servicios SOAP expuestos mediante Apache CXF.
 */
@Slf4j
@Configuration
public class WebServiceConfig {

	   private static final String WSDL_LOCATION = "classpath:/wsdl/online/x53jiServicioIntermediacion.wsdl";
	    private static final QName SERVICE_NAME = new QName("http://www.map.es/xml-schemas", "x53jiServicioOnlineIntermediacion");
	    private static final QName SYNC_PORT_NAME = new QName("http://www.map.es/xml-schemas", "IntermediacionOnlinePort");
	    private static final QName ASYNC_PORT_NAME = new QName("http://www.map.es/xml-schemas", "IntermediacionOnlineAsyncPort");

	    @Autowired
	    private AuditInputInterceptor auditInputInterceptor;

	    @Autowired
	    private AuditOutputInterceptor auditOutputInterceptor;
	@Bean(name = Bus.DEFAULT_BUS_ID)
	SpringBus springBus() {
		return new SpringBus();
	}
	  @Autowired
	    private OnlineRequestValidator onlineRequestValidator;
	  
	


	    @Bean
	    public Endpoint itTxartelaOnlineServiceEndpoint(Bus bus, IntermediacionOnlinePortType service) {
	        EndpointImpl endpoint = createEndpoint(bus, service, "/ittxartela/online", SYNC_PORT_NAME);
	        log.info("Servicio síncrono publicado en /ittxartela/online");
	        return endpoint;
	    }

	    private EndpointImpl createEndpoint(Bus bus, Object implementor, String address, QName portName) {
	        EndpointImpl endpoint = new EndpointImpl(bus, implementor);
	        endpoint.publish(address);
	        endpoint.setWsdlLocation(WSDL_LOCATION);
	        endpoint.setServiceName(SERVICE_NAME);
	        endpoint.setEndpointName(portName);
	        endpoint.getInInterceptors().add(onlineRequestValidator);
	        endpoint.getInInterceptors().add(auditInputInterceptor);
	        endpoint.getOutInterceptors().add(auditOutputInterceptor);
	        return endpoint;
	    }
	    private void addSecurityInterceptors(EndpointImpl endpoint) {
	        Map<String, Object> inProps = new HashMap<>();
	        inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP);
	        inProps.put(WSHandlerConstants.SIG_VER_PROP_FILE, "server-crypto.properties");
	        inProps.put(WSHandlerConstants.TTL_TIMESTAMP, "300");
	        endpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
	    }
	}