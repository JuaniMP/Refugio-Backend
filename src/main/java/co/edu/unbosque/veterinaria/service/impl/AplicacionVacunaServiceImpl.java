package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import co.edu.unbosque.veterinaria.repository.AplicacionVacunaRepository;
import co.edu.unbosque.veterinaria.service.api.AplicacionVacunaServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AplicacionVacunaServiceImpl extends GenericServiceImpl<AplicacionVacuna, Integer>
        implements AplicacionVacunaServiceAPI {

    @Autowired
    private AplicacionVacunaRepository aplicacionVacunaRepository;

    @Override
    public CrudRepository<AplicacionVacuna, Integer> getDao() {
        return aplicacionVacunaRepository;
    }
    @Override
    public List<AplicacionVacuna> findByIdHistorial(Integer idHistorial) {
        return aplicacionVacunaRepository.findByHistorial_IdHistorialOrderByFechaDesc(idHistorial);
    }
}
