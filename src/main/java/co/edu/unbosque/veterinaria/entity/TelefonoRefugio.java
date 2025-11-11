package co.edu.unbosque.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Telefono_Refugio")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(TelefonoRefugioId.class)
public class TelefonoRefugio {

    @Id
    @Column(name = "id_refugio")
    private Integer idRefugio;

    @Id
    @Column(name = "telefono", length = 30)
    private String telefono;
}
