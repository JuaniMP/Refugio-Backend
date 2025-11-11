package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.repository.AuditoriaRepository;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaServiceImpl extends GenericServiceImpl<Auditoria, Integer> implements AuditoriaServiceAPI {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Override
    public CrudRepository<Auditoria, Integer> getDao() {
        return auditoriaRepository;
    }
}
