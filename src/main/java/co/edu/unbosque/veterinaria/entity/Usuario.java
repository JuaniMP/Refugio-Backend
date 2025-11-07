package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "Usuario")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "login", nullable = false, unique = true, length = 60)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private Estado estado = Estado.ACTIVO;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    public enum Estado { ACTIVO, INACTIVO }
}
