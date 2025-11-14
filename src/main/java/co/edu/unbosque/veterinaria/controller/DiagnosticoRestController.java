package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Diagnostico;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.DiagnosticoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoRestController {

    @Autowired private DiagnosticoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<Diagnostico> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Diagnostico> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Diagnostico d = service.get(id);
        if (d == null) throw new ResourceNotFoundException("diagnostico no encontrado: " + id);
        return ResponseEntity.ok(d);
    }


    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Diagnostico d,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = (d.getIdDiagnostico() == null);

        // ... (validaciones existentes sin cambios) ...
        if (d.getFecha() == null) d.setFecha(LocalDateTime.now());
        if (!esNuevo) {
            Diagnostico existente = service.get(d.getIdDiagnostico());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el diagnostico con id " + d.getIdDiagnostico());
            }
        }
        if (d.getHistorial() == null || d.getHistorial().getIdHistorial() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el historial medico (idHistorial)");
        }
        if (d.getEmpleado() == null || d.getEmpleado().getIdEmpleado() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el empleado que registra el diagnostico (idEmpleado)");
        }
        if (d.getDiagnostico() == null || d.getDiagnostico().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("el campo 'diagnostico' es obligatorio");
        }

        Diagnostico guardado = service.save(d);

        // ... (lógica del comentario sin cambios) ...
        String comentario = esNuevo
                ? "se creo diagnostico " + guardado.getIdDiagnostico() + " en historial " + guardado.getHistorial().getIdHistorial()
                : "se actualizo diagnostico " + guardado.getIdDiagnostico() + " en historial " + guardado.getHistorial().getIdHistorial();

        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Diagnostico",
                guardado.getIdDiagnostico() != null ? guardado.getIdDiagnostico().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // --- 7. HELPER DE AUDITORÍA ACTUALIZADO ---
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
