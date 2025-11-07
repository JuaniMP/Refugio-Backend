package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.repository.EmpleadoRepository;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Service
public class EmpleadoServiceImpl extends GenericServiceImpl<Empleado, Long> implements EmpleadoServiceAPI {
    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Override
    public CrudRepository<Empleado, Long> getDao() {
        return empleadoRepository;
    }
}
