package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Integer> {

    // Recomendado: case-insensitive
    Optional<Usuario> findByLoginIgnoreCase(String login);

    // Si quieres mantener el nombre exacto que usa tu amiga:
    // Optional<Usuario> findByLogin(String login);
}
