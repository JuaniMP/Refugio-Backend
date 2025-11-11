package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Diagnostico;
import co.edu.unbosque.veterinaria.repository.DiagnosticoRepository;
import co.edu.unbosque.veterinaria.service.api.DiagnosticoServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticoServiceImpl extends GenericServiceImpl<Diagnostico, Integer> implements DiagnosticoServiceAPI {

    @Autowired
    private DiagnosticoRepository diagnosticoRepository;

    @Override
    public CrudRepository<Diagnostico, Integer> getDao() {
        return diagnosticoRepository;
    }
}
