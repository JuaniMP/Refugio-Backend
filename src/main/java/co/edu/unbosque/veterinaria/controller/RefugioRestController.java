package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Refugio;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.RefugioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/refugios")
public class RefugioRestController {

    @Autowired private RefugioServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<Refugio> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Refugio> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Refugio r = service.get(id);
        if (r == null) throw new ResourceNotFoundException("refugio no encontrado: " + id);
        return ResponseEntity.ok(r);
    }

    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Refugio r,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = (r.getIdRefugio() == null);

        // ... (validaciones existentes sin cambios) ...
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre del refugio es obligatorio");
        }

        Refugio guardado;
        try {
            guardado = service.save(r);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: verifique restricciones de unicidad o datos asociados");
        }

        String comentario = esNuevo
                ? "se creó el refugio '" + guardado.getNombre() + "'"
                : "se actualizó el refugio '" + guardado.getNombre() + "' (id=" + guardado.getIdRefugio() + ")";

        // --- 7. LLAMADA AL HELPER ACTUALIZADA ---
        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Refugio",
                guardado.getIdRefugio() != null ? guardado.getIdRefugio().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // --- 8. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        Refugio r = service.get(id);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe un refugio con id " + id);
        }

        try {
            service.delete(id);

            // --- 9. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Refugio",
                    id.toString(),
                    Accion.DELETE,
                    "se eliminó el refugio '" + r.getNombre() + "' (id=" + id + ")"
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: tiene datos asociados");
        }
    }

    // --- 10. HELPER DE AUDITORÍA ACTUALIZADO ---
    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Accion accion, String comentario) {
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