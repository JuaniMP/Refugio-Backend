package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "Auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Integer idAuditoria;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "tabla_afectada", nullable = false, length = 64)
    private String tablaAfectada;

    @Column(name = "id_registro", length = 64)
    private String idRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 10)
    private Accion accion;

    @Column(name = "comentario_auditoria", length = 255)
    private String comentarioAuditoria;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    public enum Accion { INSERT, UPDATE, DELETE, LOGIN, LOGOUT }
}
