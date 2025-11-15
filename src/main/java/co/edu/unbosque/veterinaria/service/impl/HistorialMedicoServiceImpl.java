package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import co.edu.unbosque.veterinaria.repository.HistorialMedicoRepository;
import co.edu.unbosque.veterinaria.service.api.HistorialMedicoServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class HistorialMedicoServiceImpl extends GenericServiceImpl<HistorialMedico, Integer>
        implements HistorialMedicoServiceAPI {

    @Autowired
    private HistorialMedicoRepository historialMedicoRepository;

    @Override
    public CrudRepository<HistorialMedico, Integer> getDao() {
        return historialMedicoRepository;
    }
    @Override
    public Optional<HistorialMedico> findByMascotaId(Integer idMascota) {
        return historialMedicoRepository.findByMascota_IdMascota(idMascota);
    }
}
