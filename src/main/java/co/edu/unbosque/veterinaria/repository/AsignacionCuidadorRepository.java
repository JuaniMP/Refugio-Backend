package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionCuidadorRepository extends CrudRepository<AsignacionCuidador, AsignacionCuidadorId> {
}
