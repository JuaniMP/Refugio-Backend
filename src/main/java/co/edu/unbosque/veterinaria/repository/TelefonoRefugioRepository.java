package co.edu.unbosque.veterinaria.repository;

import co.edu.unbosque.veterinaria.entity.TelefonoRefugio;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugioId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelefonoRefugioRepository extends CrudRepository<TelefonoRefugio, TelefonoRefugioId> {
}
