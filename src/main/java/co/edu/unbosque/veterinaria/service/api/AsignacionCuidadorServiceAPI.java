package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsignacionCuidadorServiceAPI extends GenericServiceAPI<AsignacionCuidador, AsignacionCuidadorId> {

    // --- ⬇️ MÉTODOS NUEVOS A AÑADIR ⬇️ ---

    List<AsignacionCuidador> findActivasByIdEmpleado(Integer idEmpleado);

    Optional<AsignacionCuidador> findActivaByIdMascotaAndIdEmpleado(Integer idMascota, Integer idEmpleado);

    void terminarTurno(Integer idEmpleado, LocalDate fechaFin);
}