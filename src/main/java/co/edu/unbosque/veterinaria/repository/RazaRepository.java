package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Raza;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RazaRepository extends CrudRepository<Raza, Long> {
}
