package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Adoptante;
import co.edu.unbosque.veterinaria.entity.Usuario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdoptanteRepository extends CrudRepository<Adoptante, Integer> {
    Optional<Adoptante> findByUsuario(Usuario usuario);
    // Alternativa si prefieres por id y evitar equals por entidad:
    // Optional<Adoptante> findByUsuario_IdUsuario(Integer idUsuario);
}
