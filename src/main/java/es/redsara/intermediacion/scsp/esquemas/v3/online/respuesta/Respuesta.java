
package es.redsara.intermediacion.scsp.esquemas.v3.online.respuesta;

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
 *         &lt;element ref="{http://intermediacion.redsara.es/scsp/esquemas/V3/online/respuesta}Atributos"/&gt;
 *         &lt;element ref="{http://intermediacion.redsara.es/scsp/esquemas/V3/online/respuesta}Transmisiones" minOccurs="0"/&gt;
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
    "atributos",
    "transmisiones"
})
@XmlRootElement(name = "Respuesta")
public class Respuesta {

    @XmlElement(name = "Atributos", required = true)
    protected Atributos atributos;
    @XmlElement(name = "Transmisiones")
    protected Transmisiones transmisiones;

    /**
     * Obtiene el valor de la propiedad atributos.
     * 
     * @return
     *     possible object is
     *     {@link Atributos }
     *     
     */
    public Atributos getAtributos() {
        return atributos;
    }

    /**
     * Define el valor de la propiedad atributos.
     * 
     * @param value
     *     allowed object is
     *     {@link Atributos }
     *     
     */
    public void setAtributos(Atributos value) {
        this.atributos = value;
    }

    /**
     * Obtiene el valor de la propiedad transmisiones.
     * 
     * @return
     *     possible object is
     *     {@link Transmisiones }
     *     
     */
    public Transmisiones getTransmisiones() {
        return transmisiones;
    }

    /**
     * Define el valor de la propiedad transmisiones.
     * 
     * @param value
     *     allowed object is
     *     {@link Transmisiones }
     *     
     */
    public void setTransmisiones(Transmisiones value) {
        this.transmisiones = value;
    }

}
