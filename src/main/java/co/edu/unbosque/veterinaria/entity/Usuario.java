package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@Entity
@Table(name = "Usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "login", nullable = false, unique = true, length = 60)
    private String login;

    // Llega en el registro (la clave en claro, que se hashea) pero nunca se devuelve en JSON
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 2)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    @Builder.Default
    private Estado estado = Estado.ACTIVO;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "verification_code", length = 64)
    @JsonIgnore // código de verificación / reseteo: nunca debe salir en la API
    private String verificationCode;

    @Column(name = "verification_code_expires")
    @JsonIgnore
    private Instant verificationCodeExpires;

    @Column(name = "requires_password_change", nullable = false)
    @Builder.Default
    private boolean requiresPasswordChange = false; // Para empleados nuevos

    public enum Estado {
        ACTIVO,
        INACTIVO
    }

    public enum Rol {
        AD,  // Administrador
        V,   // Veterinario
        C,   // Cuidador
        AP   // Adoptante
    }
}