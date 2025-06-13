
package es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.respuesta.datosespecificos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para DatosEspecificosOnlineRespuestaItTxartela complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="DatosEspecificosOnlineRespuestaItTxartela"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element ref="{http://intermediacion.redsara.es/scsp/esquemas/ittaxtela/online/respuesta/datosespecificos}Retorno" minOccurs="0"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DatosEspecificosOnlineRespuestaItTxartela", propOrder = {

})
public class DatosEspecificosOnlineRespuestaItTxartela {

    @XmlElement(name = "Retorno")
    protected Retorno retorno;

    /**
     * Obtiene el valor de la propiedad retorno.
     * 
     * @return
     *     possible object is
     *     {@link Retorno }
     *     
     */
    public Retorno getRetorno() {
        return retorno;
    }

    /**
     * Define el valor de la propiedad retorno.
     * 
     * @param value
     *     allowed object is
     *     {@link Retorno }
     *     
     */
    public void setRetorno(Retorno value) {
        this.retorno = value;
    }

}
