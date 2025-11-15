// Archivo: src/main/java/co/edu/unbosque/veterinaria/service/api/SolicitudAdopcionServiceAPI.java
package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;

import java.util.List; // <-- AÑADIR

public interface SolicitudAdopcionServiceAPI extends GenericServiceAPI<SolicitudAdopcion, Integer> {
    SolicitudAdopcion aprobarYGenerarAdopcion(Integer idSolicitud);

    // --- ⬇️ MÉTODO NECESARIO PARA EL ENDPOINT /by-adoptante/me ⬇️ ---
    List<SolicitudAdopcion> findByAdoptanteId(Integer idAdoptante);
}