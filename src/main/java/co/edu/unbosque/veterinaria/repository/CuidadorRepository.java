package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Cuidador;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuidadorRepository extends CrudRepository<Cuidador, Integer> {
}
