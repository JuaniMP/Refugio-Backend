package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Mascota")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mascota")
    private Integer idMascota;

    @Column(name = "nombre", length = 80, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", length = 12)
    private Sexo sexo = Sexo.DESCONOCIDO;

    @Column(name = "edad_meses")
    private Integer edadMeses;

    @ManyToOne
    @JoinColumn(name = "id_raza", nullable = false)
    private Raza raza;

    @ManyToOne
    @JoinColumn(name = "id_refugio", nullable = false)
    private Refugio refugio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private Estado estado = Estado.EN_REFUGIO;

    @Column(name = "img", length = 100)
    private String img;

    public enum Sexo { M, F, DESCONOCIDO }
    public enum Estado { EN_REFUGIO, EN_PROCESO_ADOPCION, ADOPTADA, OTRO }
}
