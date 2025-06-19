
package es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _DatosEspecificos_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/respuesta/datosespecificos", "DatosEspecificos");
    private final static QName _DatosTraza_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/respuesta/datosespecificos", "DatosTraza");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DatosEspecificosOnlineRespuestaItTxartela }
     * 
     */
    public DatosEspecificosOnlineRespuestaItTxartela createDatosEspecificosOnlineRespuestaItTxartela() {
        return new DatosEspecificosOnlineRespuestaItTxartela();
    }

    /**
     * Create an instance of {@link Traza }
     * 
     */
    public Traza createTraza() {
        return new Traza();
    }

    /**
     * Create an instance of {@link EstadoResultado }
     * 
     */
    public EstadoResultado createEstadoResultado() {
        return new EstadoResultado();
    }

    /**
     * Create an instance of {@link Retorno }
     * 
     */
    public Retorno createRetorno() {
        return new Retorno();
    }

    /**
     * Create an instance of {@link Personas }
     * 
     */
    public Personas createPersonas() {
        return new Personas();
    }

    /**
     * Create an instance of {@link Persona }
     * 
     */
    public Persona createPersona() {
        return new Persona();
    }

    /**
     * Create an instance of {@link Certificados }
     * 
     */
    public Certificados createCertificados() {
        return new Certificados();
    }

    /**
     * Create an instance of {@link Certificado }
     * 
     */
    public Certificado createCertificado() {
        return new Certificado();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DatosEspecificosOnlineRespuestaItTxartela }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DatosEspecificosOnlineRespuestaItTxartela }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/respuesta/datosespecificos", name = "DatosEspecificos")
    public JAXBElement<DatosEspecificosOnlineRespuestaItTxartela> createDatosEspecificos(DatosEspecificosOnlineRespuestaItTxartela value) {
        return new JAXBElement<DatosEspecificosOnlineRespuestaItTxartela>(_DatosEspecificos_QNAME, DatosEspecificosOnlineRespuestaItTxartela.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Traza }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Traza }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/respuesta/datosespecificos", name = "DatosTraza")
    public JAXBElement<Traza> createDatosTraza(Traza value) {
        return new JAXBElement<Traza>(_DatosTraza_QNAME, Traza.class, null, value);
    }

}
