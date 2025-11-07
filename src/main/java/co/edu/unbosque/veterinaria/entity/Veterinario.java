package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Veterinario")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Veterinario {
    @Id
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;

    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @Column(name = "registro_profesional", length = 50)
    private String registroProfesional;
}
