package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Adoptante;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.repository.AdoptanteRepository;
import co.edu.unbosque.veterinaria.service.api.AdoptanteServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdoptanteServiceImpl extends GenericServiceImpl<Adoptante, Integer> implements AdoptanteServiceAPI {

    @Autowired
    private AdoptanteRepository adoptanteRepository;

    @Override
    public CrudRepository<Adoptante, Integer> getDao() {
        return adoptanteRepository;
    }

    @Override
    public Optional<Adoptante> findByUsuario(Usuario usuario) {
        return adoptanteRepository.findByUsuario(usuario);
        // o: return adoptanteRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
    }
}
