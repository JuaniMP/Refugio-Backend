package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HistorialMedicoRepository extends CrudRepository<HistorialMedico, Integer> {

    Optional<HistorialMedico> findByMascota_IdMascota(Integer idMascota);

}
