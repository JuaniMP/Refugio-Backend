package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Adopcion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdopcionRepository extends CrudRepository<Adopcion, Long> {
}
