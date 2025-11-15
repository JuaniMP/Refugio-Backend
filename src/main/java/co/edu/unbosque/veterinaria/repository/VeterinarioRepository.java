// Archivo: src/main/java/co/edu/unbosque/veterinaria/repository/VeterinarioRepository.java
package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Usuario; // <-- AÑADIR
import co.edu.unbosque.veterinaria.entity.Veterinario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // <-- AÑADIR

@Repository
public interface VeterinarioRepository extends CrudRepository<Veterinario, Integer> {

    // --- ⬇️ MÉTODO NUEVO A AÑADIR ⬇️ ---
    Optional<Veterinario> findByEmpleado_Usuario(Usuario usuario);
}