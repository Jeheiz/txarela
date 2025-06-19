
package es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.peticion.datosespecificos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para DatosEspecificosOnlinePeticionItTxartela complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="DatosEspecificosOnlinePeticionItTxartela"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="idTraza" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Documentacion" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="FechaLimite" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="TipoCertificacion"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}int"&gt;
 *               &lt;totalDigits value="1"/&gt;
 *               &lt;minInclusive value="0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DatosEspecificosOnlinePeticionItTxartela", propOrder = {

})
public class DatosEspecificosOnlinePeticionItTxartela {

    @XmlElement(required = true)
    protected String idTraza;
    @XmlElement(name = "Documentacion", required = true)
    protected String documentacion;
    @XmlElement(name = "FechaLimite", required = true)
    protected String fechaLimite;
    @XmlElement(name = "TipoCertificacion")
    protected int tipoCertificacion;

    /**
     * Obtiene el valor de la propiedad idTraza.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdTraza() {
        return idTraza;
    }

    /**
     * Define el valor de la propiedad idTraza.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdTraza(String value) {
        this.idTraza = value;
    }

    /**
     * Obtiene el valor de la propiedad documentacion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocumentacion() {
        return documentacion;
    }

    /**
     * Define el valor de la propiedad documentacion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocumentacion(String value) {
        this.documentacion = value;
    }

    /**
     * Obtiene el valor de la propiedad fechaLimite.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFechaLimite() {
        return fechaLimite;
    }

    /**
     * Define el valor de la propiedad fechaLimite.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFechaLimite(String value) {
        this.fechaLimite = value;
    }

    /**
     * Obtiene el valor de la propiedad tipoCertificacion.
     * 
     */
    public int getTipoCertificacion() {
        return tipoCertificacion;
    }

    /**
     * Define el valor de la propiedad tipoCertificacion.
     * 
     */
    public void setTipoCertificacion(int value) {
        this.tipoCertificacion = value;
    }

}
