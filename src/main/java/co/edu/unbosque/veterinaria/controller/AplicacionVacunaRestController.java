package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.security.SesionActual;
import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.service.api.AplicacionVacunaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/aplicaciones-vacuna")
public class AplicacionVacunaRestController {

    @Autowired private AuditoriaRegistrador auditoria;
    @Autowired private SesionActual sesionActual;

    @Autowired private AplicacionVacunaServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;
    // --- ⬇️ DEPENDENCIA NECESARIA ⬇️ ---
    @Autowired private VeterinarioServiceAPI veterinarioService;

    @GetMapping("/getAll")
    public List<AplicacionVacuna> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public AplicacionVacuna get(@PathVariable Integer id) throws ResourceNotFoundException {
        AplicacionVacuna a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("aplicacion de vacuna no encontrada: " + id);
        return a;
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody AplicacionVacuna a,
                                  @RequestHeader("Authorization") String authHeader) {

        Usuario actor = sesionActual.usuario(authHeader);
        if (actor == null || actor.getRol() != Usuario.Rol.V) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado. Solo Veterinarios pueden registrar aplicaciones."));
        }

        Optional<Veterinario> vetOpt = veterinarioService.findByUsuario(actor);
        if (vetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Perfil de veterinario no encontrado para este usuario."));
        }

        // El Empleado (que es el Veterinario en este contexto)
        co.edu.unbosque.veterinaria.entity.Empleado empleado = vetOpt.get().getEmpleado();

        if (a.getIdAplicacion() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("no se permite actualizar una aplicacion de vacuna existente; no debes enviar idAplicacion");
        }
        if (a.getHistorial() == null || a.getHistorial().getIdHistorial() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el historial medico (idHistorial)");
        }
        if (a.getVacuna() == null || a.getVacuna().getIdVacuna() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar la vacuna aplicada (idVacuna)");
        }
        if (a.getFecha() == null) {
            a.setFecha(LocalDate.now());
        }

        a.setEmpleado(empleado);

        AplicacionVacuna guardada;
        try {
            guardada = service.save(a);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: verifique si la vacuna fue aplicada o restricción de BD");
        }

        auditoria.registrar(
                authHeader,
                "Aplicacion_Vacuna",
                guardada.getIdAplicacion().toString(),
                Accion.INSERT,
                "se registro una nueva aplicacion de vacuna " +
                        guardada.getVacuna().getIdVacuna() +
                        " en historial " + guardada.getHistorial().getIdHistorial()
        );

        return ResponseEntity.ok(guardada);
    }

    @GetMapping("/by-historial/{idHistorial}")
    public ResponseEntity<?> getVacunasPorHistorial(
            @PathVariable Integer idHistorial,
            @RequestHeader("Authorization") String authHeader) {

        if (sesionActual.usuario(authHeader) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        List<AplicacionVacuna> aplicaciones = service.findByIdHistorial(idHistorial);
        return ResponseEntity.ok(aplicaciones);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) {
        AplicacionVacuna a = service.get(id);
        if (a == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la aplicacion de vacuna con id " + id);
        }

        try {
            service.delete(id);
            auditoria.registrar(
                    authHeader,
                    "Aplicacion_Vacuna",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la aplicacion de vacuna con id " + id
            );
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: esta vinculada a otros registros");
        }
    }

}