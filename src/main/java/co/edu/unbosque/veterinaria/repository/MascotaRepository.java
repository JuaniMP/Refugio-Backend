package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Mascota;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- AÑADIR IMPORT

@Repository
public interface MascotaRepository extends CrudRepository<Mascota, Integer> {

    // --- ⬇️ MÉTODO NUEVO A AÑADIR ⬇️ ---
    List<Mascota> findByZonaAsignada(String zonaAsignada);
}