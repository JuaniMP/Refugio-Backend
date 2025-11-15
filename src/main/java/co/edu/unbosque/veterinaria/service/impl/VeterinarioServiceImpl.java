// Archivo: src/main/java/co/edu/unbosque/veterinaria/service/impl/VeterinarioServiceImpl.java
package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Usuario; // <-- AÑADIR
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.repository.VeterinarioRepository;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Optional; // <-- AÑADIR

@Service
public class VeterinarioServiceImpl extends GenericServiceImpl<Veterinario, Integer>
        implements VeterinarioServiceAPI {

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    @Override
    public CrudRepository<Veterinario, Integer> getDao() {
        return veterinarioRepository;
    }

    // --- ⬇️ MÉTODO NUEVO A AÑADIR ⬇️ ---
    @Override
    public Optional<Veterinario> findByUsuario(Usuario usuario) {
        return veterinarioRepository.findByEmpleado_Usuario(usuario);
    }
}