package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Raza")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Raza {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_raza")
    private Integer idRaza;

    @ManyToOne
    @JoinColumn(name = "id_especie", nullable = false)
    private Especie especie;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;
}
