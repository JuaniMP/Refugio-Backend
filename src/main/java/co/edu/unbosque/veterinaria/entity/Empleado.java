package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Empleado")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Integer idEmpleado;

    @ManyToOne
    @JoinColumn(name = "id_refugio", nullable = false)
    private Refugio refugio;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "cedula", unique = true, length = 30)
    private String cedula;

    @Column(name = "nombre", length = 120)
    private String nombre;

    @Column(name = "telefono", length = 30)
    private String telefono;
}
