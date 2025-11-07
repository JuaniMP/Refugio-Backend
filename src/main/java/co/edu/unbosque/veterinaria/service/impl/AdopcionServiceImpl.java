package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Adopcion;
import co.edu.unbosque.veterinaria.repository.AdopcionRepository;
import co.edu.unbosque.veterinaria.service.api.AdopcionServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class AdopcionServiceImpl extends GenericServiceImpl<Adopcion, Long> implements AdopcionServiceAPI {
    @Autowired
    private AdopcionRepository adopcionRepository;

    @Override
    public CrudRepository<Adopcion, Long> getDao() {
        return adopcionRepository;
    }
}
