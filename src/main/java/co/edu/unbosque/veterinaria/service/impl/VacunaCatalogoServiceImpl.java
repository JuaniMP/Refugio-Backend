package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.VacunaCatalogo;
import co.edu.unbosque.veterinaria.repository.VacunaCatalogoRepository;
import co.edu.unbosque.veterinaria.service.api.VacunaCatalogoServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class VacunaCatalogoServiceImpl extends GenericServiceImpl<VacunaCatalogo, Long>
        implements VacunaCatalogoServiceAPI {

    @Autowired
    private VacunaCatalogoRepository vacunaCatalogoRepository;

    @Override
    public CrudRepository<VacunaCatalogo, Long> getDao() {
        return vacunaCatalogoRepository;
    }
}
