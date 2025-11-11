package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.Adoptante;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;

import java.util.Optional;

public interface AdoptanteServiceAPI extends GenericServiceAPI<Adoptante, Integer> {
    Optional<Adoptante> findByUsuario(Usuario usuario);
    // o: Optional<Adoptante> findByUsuarioId(Integer idUsuario);
}
