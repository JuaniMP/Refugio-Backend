package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;
import java.util.List;

public interface AplicacionVacunaServiceAPI extends GenericServiceAPI<AplicacionVacuna, Integer> {
    List<AplicacionVacuna> findByIdHistorial(Integer idHistorial);
}
