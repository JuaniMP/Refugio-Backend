package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Diagnostico;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Veterinario; // <-- AÑADIDO
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.DiagnosticoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI; // <-- AÑADIDO
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map; // <-- AÑADIDO
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoRestController {

    @Autowired private DiagnosticoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;
    // --- ⬇️ DEPENDENCIA NECESARIA ⬇️ ---
    @Autowired private VeterinarioServiceAPI veterinarioService;

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


    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Diagnostico d,
                                  @RequestHeader("Authorization") String authHeader) {

        // --- 1. OBTENER EL ACTOR Y EL EMPLEADO ASOCIADO ---
        Usuario actor = getActorFromToken(authHeader);
        if (actor == null || actor.getRol() != Usuario.Rol.V) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado. Solo Veterinarios pueden registrar diagnósticos."));
        }

        Optional<Veterinario> vetOpt = veterinarioService.findByUsuario(actor);
        if (vetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Perfil de veterinario no encontrado para este usuario."));
        }

        // El Empleado (que es el Veterinario en este contexto)
        co.edu.unbosque.veterinaria.entity.Empleado empleado = vetOpt.get().getEmpleado();


        boolean esNuevo = (d.getIdDiagnostico() == null);

        // ... (validations) ...
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
        if (d.getDiagnostico() == null || d.getDiagnostico().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("el campo 'diagnostico' es obligatorio");
        }

        // --- 2. ASIGNAR EL EMPLEADO ANTES DE GUARDAR (LA CLAVE) ---
        d.setEmpleado(empleado);

        Diagnostico guardado;
        try {
            guardado = service.save(d);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: verifique las restricciones de la base de datos");
        }


        // ... (rest of the audit code is fine) ...
        String comentario = esNuevo
                ? "se creo diagnostico " + guardado.getIdDiagnostico() + " en historial " + guardado.getHistorial().getIdHistorial()
                : "se actualizo diagnostico " + guardado.getIdDiagnostico() + " en historial " + guardado.getHistorial().getIdHistorial();

        registrarAuditoria(
                authHeader,
                "Diagnostico",
                guardado.getIdDiagnostico() != null ? guardado.getIdDiagnostico().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // ... (Método by-historial y helper sin cambios) ...
    @GetMapping("/by-historial/{idHistorial}")
    public ResponseEntity<?> getDiagnosticosPorHistorial(
            @PathVariable Integer idHistorial,
            @RequestHeader("Authorization") String authHeader) {

        if (getActorFromToken(authHeader) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        List<Diagnostico> diagnosticos = service.findByIdHistorial(idHistorial);
        return ResponseEntity.ok(diagnosticos);
    }

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
