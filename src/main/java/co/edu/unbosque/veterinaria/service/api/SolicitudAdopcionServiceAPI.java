package co.edu.unbosque.veterinaria.service.api;

import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI; // <-- ESTE IMPORT

public interface SolicitudAdopcionServiceAPI extends GenericServiceAPI<SolicitudAdopcion, Integer> {
    SolicitudAdopcion aprobarYGenerarAdopcion(Integer idSolicitud);
}
