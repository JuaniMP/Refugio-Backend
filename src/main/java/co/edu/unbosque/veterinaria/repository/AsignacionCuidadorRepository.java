package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionCuidadorRepository extends CrudRepository<AsignacionCuidador, AsignacionCuidadorId> {

    // --- ⬇️ MÉTODOS NUEVOS A AÑADIR ⬇️ ---

    // Busca todas las asignaciones de un empleado que están activas (sin fecha_fin)
    List<AsignacionCuidador> findByIdEmpleadoAndFechaFinIsNull(Integer idEmpleado);

    // Busca UNA asignación activa para una mascota y empleado específicos
    Optional<AsignacionCuidador> findByIdMascotaAndIdEmpleadoAndFechaFinIsNull(Integer idMascota, Integer idEmpleado);

    // Cierra todos los turnos activos de un empleado (más eficiente que buscar y guardar)
    @Modifying
    @Query("UPDATE AsignacionCuidador a SET a.fechaFin = :fechaFin WHERE a.idEmpleado = :idEmpleado AND a.fechaFin IS NULL")
    void terminarTurno(@Param("idEmpleado") Integer idEmpleado, @Param("fechaFin") LocalDate fechaFin);
}