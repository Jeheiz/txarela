
package es.redsara.intermediacion.scsp.esquemas.v3.online.peticion;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the es.redsara.intermediacion.scsp.esquemas.v3.online.peticion package. 
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

    private final static QName _Apellido1_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "Apellido1");
    private final static QName _Apellido2_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "Apellido2");
    private final static QName _IdPeticion_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "IdPeticion");
    private final static QName _NumElementos_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "NumElementos");
    private final static QName _TimeStamp_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "TimeStamp");
    private final static QName _CodigoEstado_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "CodigoEstado");
    private final static QName _CodigoEstadoSecundario_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "CodigoEstadoSecundario");
    private final static QName _LiteralError_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "LiteralError");
    private final static QName _TiempoEstimadoRespuesta_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "TiempoEstimadoRespuesta");
    private final static QName _CodigoCertificado_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "CodigoCertificado");
    private final static QName _Consentimiento_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "Consentimiento");
    private final static QName _NifEmisor_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "NifEmisor");
    private final static QName _NombreEmisor_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "NombreEmisor");
    private final static QName _IdentificadorSolicitante_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "IdentificadorSolicitante");
    private final static QName _NombreSolicitante_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "NombreSolicitante");
    private final static QName _UnidadTramitadora_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "UnidadTramitadora");
    private final static QName _CodProcedimiento_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "CodProcedimiento");
    private final static QName _NombreProcedimiento_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "NombreProcedimiento");
    private final static QName _Finalidad_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "Finalidad");
    private final static QName _NombreCompletoFuncionario_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "NombreCompletoFuncionario");
    private final static QName _NifFuncionario_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "NifFuncionario");
    private final static QName _IdExpediente_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "IdExpediente");
    private final static QName _TipoDocumentacion_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "TipoDocumentacion");
    private final static QName _Documentacion_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "Documentacion");
    private final static QName _NombreCompleto_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "NombreCompleto");
    private final static QName _Nombre_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "Nombre");
    private final static QName _IdSolicitud_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "IdSolicitud");
    private final static QName _IdTransmision_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "IdTransmision");
    private final static QName _FechaGeneracion_QNAME = new QName("http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", "FechaGeneracion");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: es.redsara.intermediacion.scsp.esquemas.v3.online.peticion
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Atributos }
     * 
     */
    public Atributos createAtributos() {
        return new Atributos();
    }

    /**
     * Create an instance of {@link Estado }
     * 
     */
    public Estado createEstado() {
        return new Estado();
    }

    /**
     * Create an instance of {@link DatosGenericos }
     * 
     */
    public DatosGenericos createDatosGenericos() {
        return new DatosGenericos();
    }

    /**
     * Create an instance of {@link Emisor }
     * 
     */
    public Emisor createEmisor() {
        return new Emisor();
    }

    /**
     * Create an instance of {@link Solicitante }
     * 
     */
    public Solicitante createSolicitante() {
        return new Solicitante();
    }

    /**
     * Create an instance of {@link Procedimiento }
     * 
     */
    public Procedimiento createProcedimiento() {
        return new Procedimiento();
    }

    /**
     * Create an instance of {@link Funcionario }
     * 
     */
    public Funcionario createFuncionario() {
        return new Funcionario();
    }

    /**
     * Create an instance of {@link Titular }
     * 
     */
    public Titular createTitular() {
        return new Titular();
    }

    /**
     * Create an instance of {@link Transmision }
     * 
     */
    public Transmision createTransmision() {
        return new Transmision();
    }

    /**
     * Create an instance of {@link Peticion }
     * 
     */
    public Peticion createPeticion() {
        return new Peticion();
    }

    /**
     * Create an instance of {@link Solicitudes }
     * 
     */
    public Solicitudes createSolicitudes() {
        return new Solicitudes();
    }

    /**
     * Create an instance of {@link SolicitudTransmision }
     * 
     */
    public SolicitudTransmision createSolicitudTransmision() {
        return new SolicitudTransmision();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "Apellido1")
    public JAXBElement<String> createApellido1(String value) {
        return new JAXBElement<String>(_Apellido1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "Apellido2")
    public JAXBElement<String> createApellido2(String value) {
        return new JAXBElement<String>(_Apellido2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "IdPeticion")
    public JAXBElement<String> createIdPeticion(String value) {
        return new JAXBElement<String>(_IdPeticion_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "NumElementos")
    public JAXBElement<Integer> createNumElementos(Integer value) {
        return new JAXBElement<Integer>(_NumElementos_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "TimeStamp")
    public JAXBElement<String> createTimeStamp(String value) {
        return new JAXBElement<String>(_TimeStamp_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "CodigoEstado")
    public JAXBElement<String> createCodigoEstado(String value) {
        return new JAXBElement<String>(_CodigoEstado_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "CodigoEstadoSecundario")
    public JAXBElement<String> createCodigoEstadoSecundario(String value) {
        return new JAXBElement<String>(_CodigoEstadoSecundario_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "LiteralError")
    public JAXBElement<String> createLiteralError(String value) {
        return new JAXBElement<String>(_LiteralError_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "TiempoEstimadoRespuesta")
    public JAXBElement<Integer> createTiempoEstimadoRespuesta(Integer value) {
        return new JAXBElement<Integer>(_TiempoEstimadoRespuesta_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "CodigoCertificado")
    public JAXBElement<String> createCodigoCertificado(String value) {
        return new JAXBElement<String>(_CodigoCertificado_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "Consentimiento")
    public JAXBElement<String> createConsentimiento(String value) {
        return new JAXBElement<String>(_Consentimiento_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "NifEmisor")
    public JAXBElement<String> createNifEmisor(String value) {
        return new JAXBElement<String>(_NifEmisor_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "NombreEmisor")
    public JAXBElement<String> createNombreEmisor(String value) {
        return new JAXBElement<String>(_NombreEmisor_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "IdentificadorSolicitante")
    public JAXBElement<String> createIdentificadorSolicitante(String value) {
        return new JAXBElement<String>(_IdentificadorSolicitante_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "NombreSolicitante")
    public JAXBElement<String> createNombreSolicitante(String value) {
        return new JAXBElement<String>(_NombreSolicitante_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "UnidadTramitadora")
    public JAXBElement<String> createUnidadTramitadora(String value) {
        return new JAXBElement<String>(_UnidadTramitadora_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "CodProcedimiento")
    public JAXBElement<String> createCodProcedimiento(String value) {
        return new JAXBElement<String>(_CodProcedimiento_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "NombreProcedimiento")
    public JAXBElement<String> createNombreProcedimiento(String value) {
        return new JAXBElement<String>(_NombreProcedimiento_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "Finalidad")
    public JAXBElement<String> createFinalidad(String value) {
        return new JAXBElement<String>(_Finalidad_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "NombreCompletoFuncionario")
    public JAXBElement<String> createNombreCompletoFuncionario(String value) {
        return new JAXBElement<String>(_NombreCompletoFuncionario_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "NifFuncionario")
    public JAXBElement<String> createNifFuncionario(String value) {
        return new JAXBElement<String>(_NifFuncionario_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "IdExpediente")
    public JAXBElement<String> createIdExpediente(String value) {
        return new JAXBElement<String>(_IdExpediente_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "TipoDocumentacion")
    public JAXBElement<String> createTipoDocumentacion(String value) {
        return new JAXBElement<String>(_TipoDocumentacion_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "Documentacion")
    public JAXBElement<String> createDocumentacion(String value) {
        return new JAXBElement<String>(_Documentacion_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "NombreCompleto")
    public JAXBElement<String> createNombreCompleto(String value) {
        return new JAXBElement<String>(_NombreCompleto_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "Nombre")
    public JAXBElement<String> createNombre(String value) {
        return new JAXBElement<String>(_Nombre_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "IdSolicitud")
    public JAXBElement<String> createIdSolicitud(String value) {
        return new JAXBElement<String>(_IdSolicitud_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "IdTransmision")
    public JAXBElement<String> createIdTransmision(String value) {
        return new JAXBElement<String>(_IdTransmision_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "http://intermediacion.redsara.es/scsp/esquemas/V3/online/peticion", name = "FechaGeneracion")
    public JAXBElement<String> createFechaGeneracion(String value) {
        return new JAXBElement<String>(_FechaGeneracion_QNAME, String.class, null, value);
    }

}
