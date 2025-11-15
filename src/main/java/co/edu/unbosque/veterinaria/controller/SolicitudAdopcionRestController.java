package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Adoptante;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.SolicitudAdopcionServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AdoptanteServiceAPI; // <-- DEPENDENCIA AÑADIDA
import co.edu.unbosque.veterinaria.utils.JwtUtil;
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
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;
    @Autowired private AdoptanteServiceAPI adoptanteService; // <-- INYECCIÓN DE DEPENDENCIA


    // ================== CRUD BÁSICO ==================

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

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody SolicitudAdopcion s,
                                  @RequestHeader("Authorization") String authHeader) {
        boolean esNuevo = (s.getIdSolicitud() == null);

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

        registrarAuditoria(
                authHeader,
                "Solicitud_Adopcion",
                String.valueOf(guardada.getIdSolicitud()),
                esNuevo ? Auditoria.Accion.INSERT : Auditoria.Accion.UPDATE,
                esNuevo ? "Se creó solicitud de adopción" : "Se actualizó solicitud de adopción"
        );

        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("idSolicitud", guardada.getIdSolicitud());
        resp.put("estado", guardada.getEstado() != null ? guardada.getEstado().name() : null);
        resp.put("adoptanteId", guardada.getAdoptante() != null ? guardada.getAdoptante().getIdAdoptante() : null);
        resp.put("mascotaId",   guardada.getMascota()   != null ? guardada.getMascota().getIdMascota()   : null);
        resp.put("fechaSolicitud", guardada.getFechaSolicitud());

        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) {
        SolicitudAdopcion s = service.get(id);
        if (s == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no existe la solicitud con id " + id);
        }
        service.delete(id);

        registrarAuditoria(
                authHeader,
                "Solicitud_Adopcion",
                String.valueOf(id),
                Auditoria.Accion.DELETE,
                "Se eliminó la solicitud de adopción"
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer id,
                                     @RequestHeader("Authorization") String authHeader) {
        try {
            SolicitudAdopcion aprobada = service.aprobarYGenerarAdopcion(id);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("idSolicitud", aprobada.getIdSolicitud());
            resp.put("estado", aprobada.getEstado() != null ? aprobada.getEstado().toString() : null);
            resp.put("adoptanteId", aprobada.getAdoptante() != null ? aprobada.getAdoptante().getIdAdoptante() : null);
            resp.put("mascotaId", aprobada.getMascota() != null ? aprobada.getMascota().getIdMascota() : null);

            registrarAuditoria(
                    authHeader,
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

    // --- ⬇️ ENDPOINT NUEVO PARA EL ADOPTANTE LOGUEADO ⬇️ ---
    @GetMapping("/by-adoptante/me")
    public ResponseEntity<?> getMisSolicitudes(@RequestHeader("Authorization") String authHeader) {
        Usuario actor = getActorFromToken(authHeader);
        if (actor == null || actor.getRol() != Usuario.Rol.AP) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Acceso denegado. Solo Adoptantes."));
        }

        Optional<Adoptante> adoptanteOpt = adoptanteService.findByUsuario(actor);
        if (adoptanteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Perfil de adoptante no encontrado."));
        }

        Integer idAdoptante = adoptanteOpt.get().getIdAdoptante();
        // Llama al nuevo método en el servicio para obtener las solicitudes del adoptante
        List<SolicitudAdopcion> solicitudes = service.findByAdoptanteId(idAdoptante);

        return ResponseEntity.ok(solicitudes);
    }


    // --- HELPER DE AUDITORÍA ---
    private Usuario getActorFromToken(String authHeader) {
        try {
            String token = authHeader.substring(7); // Quita "Bearer "
            String login = jwtUtil.getLoginFromToken(token);
            Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
            return usuarioOpt.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Accion accion, String comentario) {
        Usuario actor = getActorFromToken(authHeader);

        Auditoria aud = Auditoria.builder()
                .usuario(actor)
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
