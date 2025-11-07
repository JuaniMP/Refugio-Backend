package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialMedicoRepository extends CrudRepository<HistorialMedico, Long> {
}
