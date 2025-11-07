package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends CrudRepository<Auditoria, Long> {
}
