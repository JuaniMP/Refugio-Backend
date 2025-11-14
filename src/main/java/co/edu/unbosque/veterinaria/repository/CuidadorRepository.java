// Archivo: src/main/java/co/edu/unbosque/veterinaria/repository/CuidadorRepository.java
package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- Importar
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // <-- Importar

@Repository
public interface CuidadorRepository extends CrudRepository<Cuidador, Integer> {

    // --- MÉTODO NUEVO AÑADIDO ---
    Optional<Cuidador> findByEmpleado_Usuario(Usuario usuario);
}