package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Solicitud_Adopcion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SolicitudAdopcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @ManyToOne
    @JoinColumn(name = "id_adoptante", nullable = false)
    private Adoptante adoptante;

    @ManyToOne
    @JoinColumn(name = "id_mascota", nullable = false)
    private Mascota mascota;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 12)
    private Estado estado = Estado.PENDIENTE;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    public enum Estado { PENDIENTE, APROBADA, RECHAZADA, CANCELADA }
}
