package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.SolicitudAdopcionServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudAdopcionRestController {

    @Autowired private SolicitudAdopcionServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 4. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // ================== CRUD BÁSICO ==================

    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<SolicitudAdopcion> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudAdopcion> get(@PathVariable Integer id) throws ResourceNotFoundException {
        SolicitudAdopcion s = service.get(id);
        if (s == null) throw new ResourceNotFoundException("solicitud no encontrada: " + id);
        return ResponseEntity.ok(s);
    }

    // --- 5. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody SolicitudAdopcion s,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = (s.getIdSolicitud() == null);

        // ... (Validaciones y defaults sin cambios) ...
        if (s.getAdoptante() == null || s.getAdoptante().getIdAdoptante() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar el adoptante con idAdoptante");
        }
        if (s.getMascota() == null || s.getMascota().getIdMascota() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar la mascota con idMascota");
        }
        if (s.getFechaSolicitud() == null) s.setFechaSolicitud(LocalDateTime.now());
        if (s.getEstado() == null) s.setEstado(SolicitudAdopcion.Estado.PENDIENTE);
        if (s.getObservaciones() != null) s.setObservaciones(s.getObservaciones().trim());
        if (!esNuevo && service.get(s.getIdSolicitud()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la solicitud con id " + s.getIdSolicitud());
        }

        SolicitudAdopcion guardada = service.save(s);

        // --- 6. LLAMADA AL HELPER ACTUALIZADA ---
        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Solicitud_Adopcion",
                String.valueOf(guardada.getIdSolicitud()),
                esNuevo ? Auditoria.Accion.INSERT : Auditoria.Accion.UPDATE,
                esNuevo ? "Se creó solicitud de adopción" : "Se actualizó solicitud de adopción"
        );

        // ... (Respuesta liviana sin cambios) ...
        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("idSolicitud", guardada.getIdSolicitud());
        resp.put("estado", guardada.getEstado() != null ? guardada.getEstado().name() : null);
        resp.put("adoptanteId", guardada.getAdoptante() != null ? guardada.getAdoptante().getIdAdoptante() : null);
        resp.put("mascotaId",   guardada.getMascota()   != null ? guardada.getMascota().getIdMascota()   : null);
        resp.put("fechaSolicitud", guardada.getFechaSolicitud());

        return ResponseEntity.ok(resp);
    }

    // --- 7. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        SolicitudAdopcion s = service.get(id);
        if (s == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no existe la solicitud con id " + id);
        }
        service.delete(id);

        // --- 8. LLAMADA AL HELPER ACTUALIZADA ---
        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Solicitud_Adopcion",
                String.valueOf(id),
                Auditoria.Accion.DELETE,
                "Se eliminó la solicitud de adopción"
        );

        return ResponseEntity.ok().build();
    }

    // --- 9. MÉTODO 'aprobar' ACTUALIZADO ---
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer id,
                                     @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        try {
            SolicitudAdopcion aprobada = service.aprobarYGenerarAdopcion(id);

            // ... (Respuesta liviana sin cambios) ...
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("idSolicitud", aprobada.getIdSolicitud());
            resp.put("estado", aprobada.getEstado() != null ? aprobada.getEstado().toString() : null);
            resp.put("adoptanteId", aprobada.getAdoptante() != null ? aprobada.getAdoptante().getIdAdoptante() : null);
            resp.put("mascotaId", aprobada.getMascota() != null ? aprobada.getMascota().getIdMascota() : null);

            // --- 10. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Solicitud_Adopcion",
                    String.valueOf(aprobada.getIdSolicitud()),
                    Auditoria.Accion.UPDATE,
                    "Solicitud aprobada y adopción creada"
            );

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // --- 11. HELPER DE AUDITORÍA AÑADIDO ---
    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Auditoria.Accion accion, String comentario) {
        Usuario actor = null;
        try {
            // Extraer el usuario del token
            String token = authHeader.substring(7); // Quita "Bearer "
            String login = jwtUtil.getLoginFromToken(token);
            Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
            if (usuarioOpt.isPresent()) {
                actor = usuarioOpt.get();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario para auditoría: " + e.getMessage());
        }

        Auditoria aud = Auditoria.builder()
                .usuario(actor) // <-- Se asigna el actor (o null si falló)
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
