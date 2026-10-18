package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.security.SesionActual;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Diagnostico;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.DiagnosticoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoRestController {

    @Autowired private AuditoriaRegistrador auditoria;
    @Autowired private SesionActual sesionActual;

    @Autowired private DiagnosticoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;
    // --- ⬇️ DEPENDENCIA NECESARIA ⬇️ ---
    @Autowired private VeterinarioServiceAPI veterinarioService;

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

        Usuario actor = sesionActual.usuario(authHeader);
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

        d.setEmpleado(empleado);

        Diagnostico guardado;
        try {
            guardado = service.save(d);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: verifique las restricciones de la base de datos");
        }

        String comentario = esNuevo
                ? "se creo diagnostico " + guardado.getIdDiagnostico() + " en historial " + guardado.getHistorial().getIdHistorial()
                : "se actualizo diagnostico " + guardado.getIdDiagnostico() + " en historial " + guardado.getHistorial().getIdHistorial();

        auditoria.registrar(
                authHeader,
                "Diagnostico",
                guardado.getIdDiagnostico() != null ? guardado.getIdDiagnostico().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    @GetMapping("/by-historial/{idHistorial}")
    public ResponseEntity<?> getDiagnosticosPorHistorial(
            @PathVariable Integer idHistorial,
            @RequestHeader("Authorization") String authHeader) {

        if (sesionActual.usuario(authHeader) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        List<Diagnostico> diagnosticos = service.findByIdHistorial(idHistorial);
        return ResponseEntity.ok(diagnosticos);
    }

}
