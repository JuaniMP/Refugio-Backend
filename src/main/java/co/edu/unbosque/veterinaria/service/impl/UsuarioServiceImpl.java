package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.repository.UsuarioRepository;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioServiceImpl extends GenericServiceImpl<Usuario, Integer> implements UsuarioServiceAPI {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public CrudRepository<Usuario, Integer> getDao() {
        return usuarioRepository;
    }

    @Override
    public Optional<Usuario> findByLogin(String login) {
        if (login == null) return Optional.empty();
        // Si usas el método ignore case en el repo:
        return usuarioRepository.findByLoginIgnoreCase(login.trim());
        // Si dejaste findByLogin sin ignore case:
        // return usuarioRepository.findByLogin(login.trim());
    }
}
