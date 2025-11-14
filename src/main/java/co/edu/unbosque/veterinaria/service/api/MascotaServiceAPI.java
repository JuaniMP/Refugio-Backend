package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;

import java.util.List; // <-- AÑADIR IMPORT

public interface MascotaServiceAPI extends GenericServiceAPI<Mascota, Integer> {

    // --- ⬇️ MÉTODO NUEVO A AÑADIR ⬇️ ---
    List<Mascota> findByZonaAsignada(String zonaAsignada);
}