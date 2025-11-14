package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Cuidador")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Cuidador {

    @Id
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @OneToOne(fetch = FetchType.EAGER) // sin cascade = PERSIST/ALL
    @MapsId
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;


    @Enumerated(EnumType.STRING)
    @Column(name = "turno", length = 10)
    private Turno turno;

    @Column(name = "zona_asignada", length = 100)
    private String zonaAsignada;

    public enum Turno { MAÑANA, TARDE, NOCHE }
}
