
package es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos;

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
 *       &lt;all&gt;
 *         &lt;element name="CodigoModulo" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="FechaExamen" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "Certificado")
public class Certificado {

    @XmlElement(name = "CodigoModulo", required = true)
    protected String codigoModulo;
    @XmlElement(name = "FechaExamen")
    protected String fechaExamen;

    /**
     * Obtiene el valor de la propiedad codigoModulo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodigoModulo() {
        return codigoModulo;
    }

    /**
     * Define el valor de la propiedad codigoModulo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodigoModulo(String value) {
        this.codigoModulo = value;
    }

    /**
     * Obtiene el valor de la propiedad fechaExamen.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFechaExamen() {
        return fechaExamen;
    }

    /**
     * Define el valor de la propiedad fechaExamen.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFechaExamen(String value) {
        this.fechaExamen = value;
    }

}
