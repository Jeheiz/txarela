package es.tecnalia.ittxartela.ws.server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import es.redsara.intermediacion.scsp.esquemas.ittaxtela.online.peticion.datosespecificos.DatosEspecificosOnlinePeticionItTxartela;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.DatosGenericos;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.Peticion;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.SolicitudTransmision;
import es.redsara.intermediacion.scsp.esquemas.v3.online.peticion.Titular;

public class PeticionUtils {

	public static List<String> extraerDocumentacionTitulares(Peticion peticion) {
	    if (peticion == null || peticion.getSolicitudes() == null || peticion.getSolicitudes().getSolicitudTransmision() == null) {
	        return Collections.emptyList();
	    }

	    return peticion.getSolicitudes().getSolicitudTransmision().stream()
	        .map(SolicitudTransmision::getDatosGenericos)
	        .filter(Objects::nonNull)
	        .map(DatosGenericos::getTitular)
	        .filter(Objects::nonNull)
	        .map(Titular::getDocumentacion)
	        .filter(Objects::nonNull)
	        .collect(Collectors.toList());
	}

	public static List<Titular> extraerTitularesSinDocumentacion(Peticion peticion) {
	    if (peticion == null || peticion.getSolicitudes() == null || peticion.getSolicitudes().getSolicitudTransmision() == null) {
	        return Collections.emptyList();
	    }

	    return peticion.getSolicitudes().getSolicitudTransmision().stream()
	        .map(SolicitudTransmision::getDatosGenericos)
	        .filter(Objects::nonNull)
	        .map(DatosGenericos::getTitular)
	        .filter(titular -> titular != null && (titular.getDocumentacion() == null || titular.getDocumentacion().trim().isEmpty()))
	        .collect(Collectors.toList());
	}

	public static java.sql.Date extraerFechaLimite(DatosEspecificosOnlinePeticionItTxartela datosEspecificos) throws ParseException {

		String fechaStr = datosEspecificos.getFechaLimite().trim();//"09/06/2025";
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		Date fechaUtil = formato.parse(fechaStr);
		java.sql.Date fechaSql = new java.sql.Date(fechaUtil.getTime());

		System.out.println("Fecha SQL: " + fechaSql);

		return fechaSql;
	}
}
