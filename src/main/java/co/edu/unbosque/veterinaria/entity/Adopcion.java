package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "Adopcion")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Adopcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adopcion")
    private Integer idAdopcion;

    @ManyToOne
    @JoinColumn(name = "id_mascota", nullable = false, unique = true)
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "id_adoptante", nullable = false)
    private Adoptante adoptante;

    @Column(name = "fecha_adopcion", nullable = false)
    private LocalDate fechaAdopcion;
}
