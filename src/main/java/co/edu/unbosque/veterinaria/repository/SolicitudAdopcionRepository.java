package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudAdopcionRepository extends JpaRepository<SolicitudAdopcion, Integer> { }
