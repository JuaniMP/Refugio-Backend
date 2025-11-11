package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AplicacionVacunaRepository extends CrudRepository<AplicacionVacuna, Integer> { }
