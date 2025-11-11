package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.SolicitudAdopcionServiceAPI;
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
    public ResponseEntity<?> save(@RequestBody SolicitudAdopcion s) {
        boolean esNuevo = (s.getIdSolicitud() == null);

        // Validaciones mínimas
        if (s.getAdoptante() == null || s.getAdoptante().getIdAdoptante() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar el adoptante con idAdoptante");
        }
        if (s.getMascota() == null || s.getMascota().getIdMascota() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar la mascota con idMascota");
        }

        // Defaults
        if (s.getFechaSolicitud() == null) s.setFechaSolicitud(LocalDateTime.now());
        if (s.getEstado() == null) s.setEstado(SolicitudAdopcion.Estado.PENDIENTE);
        if (s.getObservaciones() != null) s.setObservaciones(s.getObservaciones().trim());

        // Si viene id, validar existencia
        if (!esNuevo && service.get(s.getIdSolicitud()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la solicitud con id " + s.getIdSolicitud());
        }

        SolicitudAdopcion guardada = service.save(s);

        // Auditoría
        auditoriaService.save(Auditoria.builder()
                .tablaAfectada("Solicitud_Adopcion")
                .idRegistro(String.valueOf(guardada.getIdSolicitud()))
                .accion(esNuevo ? Auditoria.Accion.INSERT : Auditoria.Accion.UPDATE)
                .comentarioAuditoria(esNuevo ? "Se creó solicitud de adopción" : "Se actualizó solicitud de adopción")
                .build());

        // Respuesta liviana (evita problemas de Lazy proxies)
        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("idSolicitud", guardada.getIdSolicitud());
        resp.put("estado", guardada.getEstado() != null ? guardada.getEstado().name() : null);
        resp.put("adoptanteId", guardada.getAdoptante() != null ? guardada.getAdoptante().getIdAdoptante() : null);
        resp.put("mascotaId",   guardada.getMascota()   != null ? guardada.getMascota().getIdMascota()   : null);
        resp.put("fechaSolicitud", guardada.getFechaSolicitud());

        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        SolicitudAdopcion s = service.get(id);
        if (s == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no existe la solicitud con id " + id);
        }
        service.delete(id);

        auditoriaService.save(Auditoria.builder()
                .tablaAfectada("Solicitud_Adopcion")
                .idRegistro(String.valueOf(id))
                .accion(Auditoria.Accion.DELETE)
                .comentarioAuditoria("Se eliminó la solicitud de adopción")
                .build());

        return ResponseEntity.ok().build();
    }

    // ============== APROBAR (crea adopción y actualiza estados) ==============

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobar(@PathVariable Integer id) {
        try {
            SolicitudAdopcion aprobada = service.aprobarYGenerarAdopcion(id);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("idSolicitud", aprobada.getIdSolicitud());
            resp.put("estado", aprobada.getEstado() != null ? aprobada.getEstado().toString() : null);
            resp.put("adoptanteId", aprobada.getAdoptante() != null ? aprobada.getAdoptante().getIdAdoptante() : null);
            resp.put("mascotaId", aprobada.getMascota() != null ? aprobada.getMascota().getIdMascota() : null);

            auditoriaService.save(Auditoria.builder()
                    .tablaAfectada("Solicitud_Adopcion")
                    .idRegistro(String.valueOf(aprobada.getIdSolicitud()))
                    .accion(Auditoria.Accion.UPDATE)
                    .comentarioAuditoria("Solicitud aprobada y adopción creada")
                    .build());

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
