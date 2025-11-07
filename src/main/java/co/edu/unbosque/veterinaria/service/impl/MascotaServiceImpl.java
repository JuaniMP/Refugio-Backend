package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.repository.MascotaRepository;
import co.edu.unbosque.veterinaria.service.api.MascotaServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class MascotaServiceImpl extends GenericServiceImpl<Mascota, Long> implements MascotaServiceAPI {
    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public CrudRepository<Mascota, Long> getDao() {
        return mascotaRepository;
    }
}
