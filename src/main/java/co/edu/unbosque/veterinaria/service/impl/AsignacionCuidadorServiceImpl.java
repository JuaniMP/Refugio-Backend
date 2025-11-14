package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import co.edu.unbosque.veterinaria.repository.AsignacionCuidadorRepository;
import co.edu.unbosque.veterinaria.service.api.AsignacionCuidadorServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate; // <-- AÑADIR
import java.util.List; // <-- AÑADIR
import java.util.Optional; // <-- AÑADIR

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

    // --- ⬇️ MÉTODOS NUEVOS A AÑADIR ⬇️ ---

    @Override
    public List<AsignacionCuidador> findActivasByIdEmpleado(Integer idEmpleado) {
        return asignacionCuidadorRepository.findByIdEmpleadoAndFechaFinIsNull(idEmpleado);
    }

    @Override
    public Optional<AsignacionCuidador> findActivaByIdMascotaAndIdEmpleado(Integer idMascota, Integer idEmpleado) {
        return asignacionCuidadorRepository.findByIdMascotaAndIdEmpleadoAndFechaFinIsNull(idMascota, idEmpleado);
    }

    @Override
    public void terminarTurno(Integer idEmpleado, LocalDate fechaFin) {
        asignacionCuidadorRepository.terminarTurno(idEmpleado, fechaFin);
    }
}