package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;

import java.util.Optional;

public interface UsuarioServiceAPI extends GenericServiceAPI<Usuario, Integer> {
    // Elige uno y sé consistente con el repo:
    Optional<Usuario> findByLogin(String login);
    // Optional<Usuario> findByLoginIgnoreCase(String login);
}
