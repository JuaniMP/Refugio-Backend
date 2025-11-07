package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.TelefonoRefugio;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugioId;
import co.edu.unbosque.veterinaria.repository.TelefonoRefugioRepository;
import co.edu.unbosque.veterinaria.service.api.TelefonoRefugioServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class TelefonoRefugioServiceImpl
        extends GenericServiceImpl<TelefonoRefugio, TelefonoRefugioId>
        implements TelefonoRefugioServiceAPI {

    @Autowired
    private TelefonoRefugioRepository telefonoRefugioRepository;

    @Override
    public CrudRepository<TelefonoRefugio, TelefonoRefugioId> getDao() {
        return telefonoRefugioRepository;
    }
}
