package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.repository.CuidadorRepository;
import co.edu.unbosque.veterinaria.service.api.CuidadorServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class CuidadorServiceImpl extends GenericServiceImpl<Cuidador, Integer> implements CuidadorServiceAPI {

    @Autowired
    private CuidadorRepository cuidadorRepository;

    @Override
    public CrudRepository<Cuidador, Integer> getDao() {
        return cuidadorRepository;
    }
}
