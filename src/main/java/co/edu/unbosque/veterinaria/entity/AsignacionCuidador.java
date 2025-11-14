package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "Asignacion_Cuidador")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(AsignacionCuidadorId.class)
public class AsignacionCuidador {

    @Id
    @Column(name = "id_mascota")
    private Integer idMascota;

    @Id
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @Id
    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    // --- ⬇️ CAMPO NUEVO A AÑADIR ⬇️ ---
    @Column(name = "comentarios", columnDefinition = "text")
    private String comentarios;
}
