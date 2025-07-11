
package es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.peticion.datosespecificos;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.peticion.datosespecificos package. 
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

    private final static QName _DatosEspecificos_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/peticion/datosespecificos", "DatosEspecificos");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.peticion.datosespecificos
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DatosEspecificosOnlinePeticionItTxartela }
     * 
     */
    public DatosEspecificosOnlinePeticionItTxartela createDatosEspecificosOnlinePeticionItTxartela() {
        return new DatosEspecificosOnlinePeticionItTxartela();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DatosEspecificosOnlinePeticionItTxartela }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DatosEspecificosOnlinePeticionItTxartela }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/peticion/datosespecificos", name = "DatosEspecificos")
    public JAXBElement<DatosEspecificosOnlinePeticionItTxartela> createDatosEspecificos(DatosEspecificosOnlinePeticionItTxartela value) {
        return new JAXBElement<DatosEspecificosOnlinePeticionItTxartela>(_DatosEspecificos_QNAME, DatosEspecificosOnlinePeticionItTxartela.class, null, value);
    }

}
