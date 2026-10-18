package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Especie")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Especie implements Catalogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_especie")
    private Integer idEspecie;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    // --- CAMPO AÑADIDO ---
    // Columna para el estado "ACTIVO" o "INACTIVO"
    @Column(name = "estado", length = 10, nullable = false)
    @Builder.Default // Asegura que el valor por defecto se use con @Builder
    private String estado = "ACTIVO";

    @Override
    public Integer idCatalogo() {
        return idEspecie;
    }
}
