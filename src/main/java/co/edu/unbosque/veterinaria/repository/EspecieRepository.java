package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Especie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecieRepository extends CrudRepository<Especie, Long> {
}
