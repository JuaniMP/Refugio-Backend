package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Telefono_Refugio")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(TelefonoRefugioId.class) // <-- Esta línea usa el archivo que tú pegaste
public class TelefonoRefugio {

    @Id
    @Column(name = "id_refugio")
    private Integer idRefugio;

    @Id
    @Column(name = "telefono", length = 30)
    private String telefono;

    // --- CAMPO AÑADIDO ---
    // Columna para el estado "ACTIVO" o "INACTIVO"
    @Column(name = "estado", length = 10, nullable = false)
    @Builder.Default // Asegura que el valor por defecto se use con @Builder
    private String estado = "ACTIVO";
}