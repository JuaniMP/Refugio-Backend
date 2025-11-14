package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant; // <-- IMPORTAR

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

    // ... (login, passwordHash, rol, estado, creadoEn sin cambios) ...
    @Column(name = "login", nullable = false, unique = true, length = 60)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 2)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private Estado estado = Estado.ACTIVO;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    // --- CAMPOS AÑADIDOS ---
    @Column(name = "verification_code", length = 64)
    private String verificationCode; // Para reseteo de contraseña

    @Column(name = "verification_code_expires")
    private Instant verificationCodeExpires; // Expiración del código

    @Column(name = "requires_password_change", nullable = false)
    @Builder.Default
    private boolean requiresPasswordChange = false; // Para empleados nuevos
    // --- FIN DE CAMPOS AÑADIDOS ---

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