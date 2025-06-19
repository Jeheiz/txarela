package es.tecnalia.ittxartela.ws.server.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.tecnalia.ittxartela.ws.server.model.Matricula;

public interface MatriculaRepository extends JpaRepository<Matricula, String> {

	@Query(
			"SELECT m FROM Matricula m " +
			"WHERE m.dni = :dni " +
			"AND m.id_status = 1 " +
			"AND m.id_nivel = 1 " +
			"AND (:plataforma = 2 OR m.plataforma = :plataforma) " +
			"AND m.fecha_exam <= :fecha")
	List<Matricula> findExamenesRealizadosYSuperados(
			@Param("dni") String dni,
			@Param("plataforma") Integer plataforma,
			@Param("fecha") Date fecha
			);

}
