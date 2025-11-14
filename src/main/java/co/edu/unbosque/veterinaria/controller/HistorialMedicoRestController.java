package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.HistorialMedicoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/historiales")
public class HistorialMedicoRestController {

    @Autowired private HistorialMedicoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<HistorialMedico> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<HistorialMedico> get(@PathVariable Integer id) throws ResourceNotFoundException {
        HistorialMedico h = service.get(id);
        if (h == null) throw new ResourceNotFoundException("historial no encontrado: " + id);
        return ResponseEntity.ok(h);
    }

    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody HistorialMedico h,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = esInsert(h);

        // ... (validaciones existentes sin cambios) ...
        if (h.getMascota() == null || h.getMascota().getIdMascota() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar la mascota (idMascota)");
        }
        if (h.getNotas() != null) h.setNotas(h.getNotas().trim());
        if (!esNuevo) {
            HistorialMedico existente = service.get(h.getIdHistorial());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el historial con id " + h.getIdHistorial());
            }
        }

        HistorialMedico guardado = service.save(h);

        // ... (comentario de auditoría sin cambios) ...
        String comentario = esNuevo
                ? "se creo historial para la mascota " + h.getMascota().getIdMascota()
                : "se actualizo historial " + guardado.getIdHistorial() + " de la mascota " + h.getMascota().getIdMascota();

        // --- 7. LLAMADA AL HELPER ACTUALIZADA ---
        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Historial_Medico",
                guardado.getIdHistorial() != null ? guardado.getIdHistorial().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // ================= helpers internos =================

    // ... (esInsert sin cambios) ...
    private boolean esInsert(HistorialMedico h) {
        if (h.getIdHistorial() == null) return true;
        return service.get(h.getIdHistorial()) == null;
    }

    // --- 8. HELPER DE AUDITORÍA ACTUALIZADO ---
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