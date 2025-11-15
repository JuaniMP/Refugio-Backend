// Archivo: src/main/java/co/edu/unbosque/veterinaria/repository/SolicitudAdopcionRepository.java
package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- AÑADIR

@Repository
public interface SolicitudAdopcionRepository extends JpaRepository<SolicitudAdopcion, Integer> {
    // sirve para validar que una mascota no se adopte dos veces
    // boolean existsByMascota_IdMascota(Integer idMascota);

    // --- ⬇️ MÉTODO NECESARIO PARA EL ENDPOINT /by-adoptante/me ⬇️ ---
    List<SolicitudAdopcion> findByAdoptante_IdAdoptante(Integer idAdoptante);
}