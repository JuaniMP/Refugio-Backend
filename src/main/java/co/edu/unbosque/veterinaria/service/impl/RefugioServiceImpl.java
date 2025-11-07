package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Refugio;
import co.edu.unbosque.veterinaria.repository.RefugioRepository;
import co.edu.unbosque.veterinaria.service.api.RefugioServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class RefugioServiceImpl extends GenericServiceImpl<Refugio, Long> implements RefugioServiceAPI {
    @Autowired
    private RefugioRepository refugioRepository;

    @Override
    public CrudRepository<Refugio, Long> getDao() {
        return refugioRepository;
    }
}