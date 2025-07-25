
package es.redsara.intermediacion.scsp.esquemas.v3.online.peticion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.peticion.datosespecificos.DatosEspecificosOnlinePeticionItTxartela;


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
 *         &lt;element ref="{http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion}DatosGenericos"/&gt;
 *         &lt;element ref="{http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/peticion/datosespecificos}DatosEspecificos" minOccurs="0"/&gt;
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
@XmlRootElement(name = "SolicitudTransmision")
public class SolicitudTransmision {

    @XmlElement(name = "DatosGenericos", required = true)
    protected DatosGenericos datosGenericos;
    @XmlElement(name = "DatosEspecificos", namespace = "http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/peticion/datosespecificos")
    protected DatosEspecificosOnlinePeticionItTxartela datosEspecificos;

    /**
     * Obtiene el valor de la propiedad datosGenericos.
     * 
     * @return
     *     possible object is
     *     {@link DatosGenericos }
     *     
     */
    public DatosGenericos getDatosGenericos() {
        return datosGenericos;
    }

    /**
     * Define el valor de la propiedad datosGenericos.
     * 
     * @param value
     *     allowed object is
     *     {@link DatosGenericos }
     *     
     */
    public void setDatosGenericos(DatosGenericos value) {
        this.datosGenericos = value;
    }

    /**
     * Obtiene el valor de la propiedad datosEspecificos.
     * 
     * @return
     *     possible object is
     *     {@link DatosEspecificosOnlinePeticionItTxartela }
     *     
     */
    public DatosEspecificosOnlinePeticionItTxartela getDatosEspecificos() {
        return datosEspecificos;
    }

    /**
     * Define el valor de la propiedad datosEspecificos.
     * 
     * @param value
     *     allowed object is
     *     {@link DatosEspecificosOnlinePeticionItTxartela }
     *     
     */
    public void setDatosEspecificos(DatosEspecificosOnlinePeticionItTxartela value) {
        this.datosEspecificos = value;
    }

}
