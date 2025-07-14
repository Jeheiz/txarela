package es.tecnalia.ittxartela.ws.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit")
public class Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_peticion", nullable = false)
    private String idPeticion;

    @Lob
    @Column(name = "xml_peticion")
    private String xmlPeticion;

    @Lob
    @Column(name = "xml_respuesta")
    private String xmlRespuesta;
}
