package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudAdopcionRepository extends CrudRepository<SolicitudAdopcion, Long> {
}
