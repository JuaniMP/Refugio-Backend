package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Especie")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Especie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especie")
    private Integer idEspecie;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}
