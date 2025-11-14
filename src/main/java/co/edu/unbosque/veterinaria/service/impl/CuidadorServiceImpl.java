// Archivo: src/main/java/co/edu/unbosque/veterinaria/service/impl/CuidadorServiceImpl.java
package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- Importar
import co.edu.unbosque.veterinaria.repository.CuidadorRepository;
import co.edu.unbosque.veterinaria.service.api.CuidadorServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Optional; // <-- Importar

@Service
public class CuidadorServiceImpl extends GenericServiceImpl<Cuidador, Integer> implements CuidadorServiceAPI {

    @Autowired
    private CuidadorRepository cuidadorRepository;

    @Override
    public CrudRepository<Cuidador, Integer> getDao() {
        return cuidadorRepository;
    }

    // --- MÉTODO NUEVO AÑADIDO ---
    @Override
    public Optional<Cuidador> findByUsuario(Usuario usuario) {
        return cuidadorRepository.findByEmpleado_Usuario(usuario);
    }
}