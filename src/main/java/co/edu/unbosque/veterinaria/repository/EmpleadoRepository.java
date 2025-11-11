package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Empleado;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends CrudRepository<Empleado, Integer> { }
