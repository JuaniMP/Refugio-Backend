package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.repository.UsuarioRepository;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl extends GenericServiceImpl<Usuario, Long> implements UsuarioServiceAPI {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public CrudRepository<Usuario, Long> getDao() {
        return usuarioRepository;
    }
}
