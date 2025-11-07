package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "Aplicacion_Vacuna")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AplicacionVacuna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aplicacion")
    private Integer idAplicacion;

    @ManyToOne
    @JoinColumn(name = "id_historial", nullable = false)
    private HistorialMedico historial;

    @ManyToOne
    @JoinColumn(name = "id_vacuna", nullable = false)
    private VacunaCatalogo vacuna;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "id_empleado")
    private Empleado empleado; // puede ser null

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;
}
