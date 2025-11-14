package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "Mascota")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mascota {

    public enum Sexo { M, F, DESCONOCIDO }
    public enum Estado { EN_REFUGIO, EN_PROCESO_ADOPCION, ADOPTADA, OTRO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mascota")
    private Integer idMascota;

    // --- CAMBIO AQUÍ ---
    @ManyToOne(fetch = FetchType.EAGER) // De LAZY a EAGER
    @JoinColumn(name = "id_refugio", nullable = false)
    private Refugio refugio;

    // OJO: la especie se deduce por la raza. La tabla Mascota NO tiene id_especie
    // --- CAMBIO AQUÍ ---
    @ManyToOne(fetch = FetchType.EAGER) // De LAZY a EAGER
    @JoinColumn(name = "id_raza", nullable = false)
    private Raza raza;

    @Column(name = "nombre", length = 80, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo")
    private Sexo sexo; // valores: M, F, DESCONOCIDO

    @Column(name = "edad_meses")
    private Integer edadMeses;

    @Column(name = "img", length = 100)
    private String img;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 25)
    private Estado estado; // EN_REFUGIO, EN_PROCESO_ADOPCION, ADOPTADA, OTRO
}