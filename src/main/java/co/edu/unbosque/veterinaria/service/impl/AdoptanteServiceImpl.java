package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Adoptante;
import co.edu.unbosque.veterinaria.repository.AdoptanteRepository;
import co.edu.unbosque.veterinaria.service.api.AdoptanteServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class AdoptanteServiceImpl extends GenericServiceImpl<Adoptante, Long> implements AdoptanteServiceAPI {
    @Autowired
    private AdoptanteRepository adoptanteRepository;

    @Override
    public CrudRepository<Adoptante, Long> getDao() {
        return adoptanteRepository;
    }
}
