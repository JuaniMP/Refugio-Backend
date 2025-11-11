package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.Mascota;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MascotaRepository extends CrudRepository<Mascota, Integer> { }
