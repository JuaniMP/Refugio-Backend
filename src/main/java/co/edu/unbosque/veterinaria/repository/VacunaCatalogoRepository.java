package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.VacunaCatalogo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacunaCatalogoRepository extends CrudRepository<VacunaCatalogo, Integer> { }
