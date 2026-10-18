package co.edu.unbosque.veterinaria.security;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import org.springframework.stereotype.Component;

/** Registra en la tabla Auditoria quién hizo qué. Compartido por todos los controladores. */
@Component
public class AuditoriaRegistrador {

    private final AuditoriaServiceAPI auditoriaService;
    private final SesionActual sesionActual;

    public AuditoriaRegistrador(AuditoriaServiceAPI auditoriaService, SesionActual sesionActual) {
        this.auditoriaService = auditoriaService;
        this.sesionActual = sesionActual;
    }

    /** El actor se toma del token; si no hay sesión (p. ej. un registro público) queda en null. */
    public void registrar(String authHeader, String tabla, String idRegistro,
                          Auditoria.Accion accion, String comentario) {
        registrar(sesionActual.usuario(authHeader), tabla, idRegistro, accion, comentario);
    }

    public void registrar(Usuario actor, String tabla, String idRegistro,
                          Auditoria.Accion accion, String comentario) {
        auditoriaService.save(Auditoria.builder()
                .usuario(actor)
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build());
    }
}
