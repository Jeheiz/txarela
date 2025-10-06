package es.tecnalia.ittxartela.ws.server.config;

import java.util.HashMap;
import java.util.Map;
import es.map.xml_schemas.IntermediacionOnlineAsyncPortType;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import org.springframework.beans.factory.annotation.Value;
import es.map.xml_schemas.IntermediacionOnlineAsyncPortType;
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
import es.map.xml_schemas.IntermediacionOnlineAsyncPortType;

@Slf4j
@Configuration
public class WebServiceConfig {

    private static final String WSDL_LOCATION = "classpath:/wsdl/online/x53jiServicioIntermediacion.wsdl";
    private static final QName SERVICE_NAME = new QName("http://www.map.es/xml-schemas", "x53jiServicioOnlineIntermediacion");
    private static final QName SYNC_PORT_NAME = new QName("http://www.map.es/xml-schemas", "IntermediacionOnlinePort");
    private static final QName ASYNC_PORT_NAME = new QName("http://www.map.es/xml-schemas", "IntermediacionOnlineAsyncPort");

    @Value("${cxf.path:/}")
    private String cxfBasePath;

    @Autowired
    private AuditInputInterceptor auditInputInterceptor;

    @Autowired
    private AuditOutputInterceptor auditOutputInterceptor;

    @Autowired
    private OnlineRequestValidator onlineRequestValidator;

    @Bean(name = Bus.DEFAULT_BUS_ID)
    SpringBus springBus() {
        return new SpringBus();
    }

    /** Publica el servicio SÍNCRONO */
    @Bean
    public Endpoint itTxartelaOnlineServiceEndpoint(Bus bus, IntermediacionOnlinePortType service) {
        String relativeAddress = "/online";
        EndpointImpl endpoint = createEndpoint(bus, service, relativeAddress, SYNC_PORT_NAME);
        log.info("Servicio síncrono publicado en {}", buildPublishedUrl(relativeAddress));
        return endpoint;
    }

    /** Publica el servicio ASÍNCRONO */
    @Bean
    public Endpoint itTxartelaOnlineAsyncServiceEndpoint(Bus bus, IntermediacionOnlineAsyncPortType service) {
        String relativeAddress = "/online/async"; // ✅ cambiado a ruta coherente con WSDL
        EndpointImpl endpoint = createEndpoint(bus, service, relativeAddress, ASYNC_PORT_NAME);
        log.info("Servicio asíncrono publicado en {}", buildPublishedUrl(relativeAddress));
        return endpoint;
    }

    /** Configura cada endpoint con interceptores y seguridad */
    private EndpointImpl createEndpoint(Bus bus, Object implementor, String relativeAddress, QName portName) {
        String normalizedAddress = ensureLeadingSlash(relativeAddress);

        EndpointImpl endpoint = new EndpointImpl(bus, implementor);
        endpoint.publish(normalizedAddress);
        endpoint.setWsdlLocation(WSDL_LOCATION);
        endpoint.setServiceName(SERVICE_NAME);
        endpoint.setEndpointName(portName);
        endpoint.setPublishedEndpointUrl(buildPublishedUrl(normalizedAddress));
        endpoint.getInInterceptors().add(onlineRequestValidator);
        endpoint.getInInterceptors().add(auditInputInterceptor);
        endpoint.getOutInterceptors().add(auditOutputInterceptor);
        addSecurityInterceptors(endpoint);
        return endpoint;
    }

    /** Configura seguridad WS-Security */
    private void addSecurityInterceptors(EndpointImpl endpoint) {
        Map<String, Object> inProps = new HashMap<>();
        inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE + " " + WSHandlerConstants.TIMESTAMP);
        inProps.put(WSHandlerConstants.SIG_VER_PROP_FILE, "server-crypto.properties");
        inProps.put(WSHandlerConstants.TTL_TIMESTAMP, "300");
        endpoint.getInInterceptors().add(new WSS4JInInterceptor(inProps));
    }

    private String buildPublishedUrl(String relativeAddress) {
        String normalizedRelative = ensureLeadingSlash(relativeAddress);
        String base = normalizeBasePath(cxfBasePath);
        if ("/".equals(base)) {
            return normalizedRelative;
        }
        if (normalizedRelative.equals("/")) {
            return base;
        }
        return base.endsWith("/")
                ? base.substring(0, base.length() - 1) + normalizedRelative
                : base + normalizedRelative;
    }

    private String ensureLeadingSlash(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String normalizeBasePath(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            return "/";
        }
        String normalized = basePath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}