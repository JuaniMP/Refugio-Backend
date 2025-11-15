package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.Diagnostico;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;
import java.util.List;

public interface DiagnosticoServiceAPI extends GenericServiceAPI<Diagnostico, Integer> {
    List<Diagnostico> findByIdHistorial(Integer idHistorial);
}
