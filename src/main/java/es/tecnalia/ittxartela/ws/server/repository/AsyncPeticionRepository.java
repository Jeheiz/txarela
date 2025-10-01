package es.tecnalia.ittxartela.ws.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.tecnalia.ittxartela.ws.server.model.AsyncPeticion;

public interface AsyncPeticionRepository extends JpaRepository<AsyncPeticion, Long> {
    AsyncPeticion findByIdPeticion(String idPeticion);
}