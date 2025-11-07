package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Adoptante")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Adoptante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adoptante")
    private Integer idAdoptante;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre;

    @Column(name = "documento", unique = true, length = 30)
    private String documento;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "telefono", length = 30)
    private String telefono;
}
