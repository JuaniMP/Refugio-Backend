package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Vacuna_Catalogo")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VacunaCatalogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vacuna")
    private Integer idVacuna;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;
}
