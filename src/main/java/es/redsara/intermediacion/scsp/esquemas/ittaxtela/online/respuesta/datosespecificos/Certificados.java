
package es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para anonymous complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/respuesta/datosespecificos}Certificado" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "certificado"
})
@XmlRootElement(name = "Certificados")
public class Certificados {

    @XmlElement(name = "Certificado")
    protected List<Certificado> certificado;

    /**
     * Gets the value of the certificado property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the certificado property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCertificado().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Certificado }
     * 
     * 
     */
    public List<Certificado> getCertificado() {
        if (certificado == null) {
            certificado = new ArrayList<Certificado>();
        }
        return this.certificado;
    }

}
