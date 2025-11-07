package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import co.edu.unbosque.veterinaria.repository.SolicitudAdopcionRepository;
import co.edu.unbosque.veterinaria.service.api.SolicitudAdopcionServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class SolicitudAdopcionServiceImpl extends GenericServiceImpl<SolicitudAdopcion, Long>
        implements SolicitudAdopcionServiceAPI {

    @Autowired
    private SolicitudAdopcionRepository solicitudAdopcionRepository;

    @Override
    public CrudRepository<SolicitudAdopcion, Long> getDao() {
        return solicitudAdopcionRepository;
    }
}
