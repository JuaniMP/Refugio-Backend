package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Refugio;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefugioRepository extends CrudRepository<Refugio, Integer> { }
