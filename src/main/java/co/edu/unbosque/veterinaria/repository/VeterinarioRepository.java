package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Veterinario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeterinarioRepository extends CrudRepository<Veterinario, Long> {
}
