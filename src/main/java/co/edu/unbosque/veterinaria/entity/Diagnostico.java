package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Diagnostico")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Diagnostico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diagnostico")
    private Integer idDiagnostico;

    @ManyToOne
    @JoinColumn(name = "id_historial", nullable = false)
    private HistorialMedico historial;

    @ManyToOne
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado; // idealmente veterinario

    @Column(name = "diagnostico", columnDefinition = "text", nullable = false)
    private String diagnostico;

    @Column(name = "tratamiento", columnDefinition = "text")
    private String tratamiento;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "estado_salud", length = 50)
    private String estadoSalud;

    @Column(name = "fecha")
    private LocalDateTime fecha;
}
