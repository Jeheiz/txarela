package es.tecnalia.ittxartela.ws.server.util;

import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class XmlUtil {

	/**
	 * Convierte un objeto Java a una cadena XML formateada.
	 *
	 * @param objeto El objeto a convertir.
	 * @return Una cadena XML representando el objeto.
	 * @throws JAXBException Si ocurre un error durante la conversión.
	 */
	public static String toXml(Object objeto) {

		if (objeto == null) {
			throw new IllegalArgumentException("El objeto no puede ser null");
		}

		StringWriter writer = new StringWriter();

		try {

			JAXBContext context = JAXBContext.newInstance(objeto.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			marshaller.marshal(objeto, writer);

		} catch (JAXBException e) {

			writer.write("<error al convertir a XML: " + e.getMessage() + ">");
		}

		return writer.toString();
	}

	public static String obtenerFechaHoraXml() {

		ZonedDateTime ahora = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        return ahora.format(formatter);
    }
}
