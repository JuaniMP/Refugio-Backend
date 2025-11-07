package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Especie;
import co.edu.unbosque.veterinaria.repository.EspecieRepository;
import co.edu.unbosque.veterinaria.service.api.EspecieServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class EspecieServiceImpl extends GenericServiceImpl<Especie, Long> implements EspecieServiceAPI {
    @Autowired
    private EspecieRepository especieRepository;

    @Override
    public CrudRepository<Especie, Long> getDao() {
        return especieRepository;
    }
}
