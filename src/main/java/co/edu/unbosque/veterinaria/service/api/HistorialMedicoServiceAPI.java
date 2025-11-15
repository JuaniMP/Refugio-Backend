package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;
import java.util.Optional;

public interface HistorialMedicoServiceAPI extends GenericServiceAPI<HistorialMedico, Integer> {
    Optional<HistorialMedico> findByMascotaId(Integer idMascota);
}
