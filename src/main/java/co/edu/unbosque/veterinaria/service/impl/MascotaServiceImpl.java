package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.repository.MascotaRepository;
import co.edu.unbosque.veterinaria.service.api.MascotaServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MascotaServiceImpl extends GenericServiceImpl<Mascota, Integer> implements MascotaServiceAPI {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public CrudRepository<Mascota, Integer> getDao() {
        return mascotaRepository;
    }
    @Override
    public List<Mascota> findByZonaAsignada(String zonaAsignada) {
        return mascotaRepository.findByZonaAsignada(zonaAsignada);
    }
}
