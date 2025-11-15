// Archivo: src/main/java/co/edu/unbosque/veterinaria/service/api/VeterinarioServiceAPI.java
package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.Usuario; // <-- AÑADIR
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;

import java.util.Optional; // <-- AÑADIR

public interface VeterinarioServiceAPI extends GenericServiceAPI<Veterinario, Integer> {

    // --- ⬇️ MÉTODO NUEVO A AÑADIR ⬇️ ---
    Optional<Veterinario> findByUsuario(Usuario usuario);
}