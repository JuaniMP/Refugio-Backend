package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Diagnostico;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticoRepository extends CrudRepository<Diagnostico, Integer> { }
