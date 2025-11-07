package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import co.edu.unbosque.veterinaria.repository.AplicacionVacunaRepository;
import co.edu.unbosque.veterinaria.service.api.AplicacionVacunaServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class AplicacionVacunaServiceImpl extends GenericServiceImpl<AplicacionVacuna, Long>
        implements AplicacionVacunaServiceAPI {

    @Autowired
    private AplicacionVacunaRepository aplicacionVacunaRepository;

    @Override
    public CrudRepository<AplicacionVacuna, Long> getDao() {
        return aplicacionVacunaRepository;
    }
}