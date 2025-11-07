package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import co.edu.unbosque.veterinaria.repository.AsignacionCuidadorRepository;
import co.edu.unbosque.veterinaria.service.api.AsignacionCuidadorServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class AsignacionCuidadorServiceImpl
        extends GenericServiceImpl<AsignacionCuidador, AsignacionCuidadorId>
        implements AsignacionCuidadorServiceAPI {

    @Autowired
    private AsignacionCuidadorRepository asignacionCuidadorRepository;

    @Override
    public CrudRepository<AsignacionCuidador, AsignacionCuidadorId> getDao() {
        return asignacionCuidadorRepository;
    }
}
