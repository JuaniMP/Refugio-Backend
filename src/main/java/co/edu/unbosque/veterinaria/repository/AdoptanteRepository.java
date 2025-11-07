package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Adoptante;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdoptanteRepository extends CrudRepository<Adoptante, Long> {
}
