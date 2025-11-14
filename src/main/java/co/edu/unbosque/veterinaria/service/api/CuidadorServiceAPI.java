package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.entity.Usuario; // <
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;
import java.util.Optional; // <-- Import

public interface CuidadorServiceAPI extends GenericServiceAPI<Cuidador, Integer> {

    // --- MÉTODO NUEVO AÑADIDO ---
    Optional<Cuidador> findByUsuario(Usuario usuario);
}