package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import co.edu.unbosque.veterinaria.repository.HistorialMedicoRepository;
import co.edu.unbosque.veterinaria.service.api.HistorialMedicoServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class HistorialMedicoServiceImpl extends GenericServiceImpl<HistorialMedico, Long>
        implements HistorialMedicoServiceAPI {

    @Autowired
    private HistorialMedicoRepository historialMedicoRepository;

    @Override
    public CrudRepository<HistorialMedico, Long> getDao() {
        return historialMedicoRepository;
    }
}
