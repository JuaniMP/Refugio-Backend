package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Historial_Medico")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialMedico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @OneToOne
    @JoinColumn(name = "id_mascota", nullable = false, unique = true)
    private Mascota mascota;

    @Column(name = "notas", columnDefinition = "text")
    private String notas;
}
