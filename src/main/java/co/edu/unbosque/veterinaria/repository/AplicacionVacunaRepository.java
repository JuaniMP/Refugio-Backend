package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AplicacionVacunaRepository extends CrudRepository<AplicacionVacuna, Integer> {
    List<AplicacionVacuna> findByHistorial_IdHistorialOrderByFechaDesc(Integer idHistorial);
}
