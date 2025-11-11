package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Raza;
import co.edu.unbosque.veterinaria.repository.RazaRepository;
import co.edu.unbosque.veterinaria.service.api.RazaServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class RazaServiceImpl extends GenericServiceImpl<Raza, Integer> implements RazaServiceAPI {

    @Autowired
    private RazaRepository razaRepository;

    @Override
    public CrudRepository<Raza, Integer> getDao() {
        return razaRepository;
    }
}
