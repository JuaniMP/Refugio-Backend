package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.security.SesionActual;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioRestController {

    @Autowired private AuditoriaRegistrador auditoria;
    @Autowired private SesionActual sesionActual;

    @Autowired private VeterinarioServiceAPI service;
    @Autowired private EmpleadoServiceAPI empleadoService;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    @GetMapping("/getAll")
    public List<Veterinario> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Veterinario> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Veterinario v = service.get(id);
        if (v == null) throw new ResourceNotFoundException("veterinario no encontrado: " + id);
        return ResponseEntity.ok(v);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Veterinario body,
                                  @RequestHeader("Authorization") String authHeader) {

        if (body.getEmpleado() == null || body.getEmpleado().getIdEmpleado() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el empleado con su id");
        }
        Integer empId = body.getEmpleado().getIdEmpleado();
        Empleado emp = empleadoService.get(empId);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe el empleado con id " + empId);
        }
        Veterinario existente = service.get(empId);
        final boolean esNuevo = (existente == null);
        Veterinario target;
        if (esNuevo) {
            target = new Veterinario();
            target.setEmpleado(emp);
        } else {
            target = existente;
        }
        if (body.getEspecialidad() != null)
            target.setEspecialidad(body.getEspecialidad().trim());
        if (body.getRegistroProfesional() != null)
            target.setRegistroProfesional(body.getRegistroProfesional().trim());

        try {
            Veterinario guardado = service.save(target);

            auditoria.registrar(
                    authHeader,
                    "Veterinario",
                    guardado.getIdEmpleado().toString(),
                    esNuevo ? Auditoria.Accion.INSERT : Auditoria.Accion.UPDATE,
                    esNuevo
                            ? "se creo veterinario para el empleado " + guardado.getIdEmpleado()
                            : "se actualizo veterinario " + guardado.getIdEmpleado()
            );
            return ResponseEntity.ok(guardado);

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: restriccion de integridad (FK/UK)");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) {
        Veterinario v = service.get(id);
        if (v == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no existe el veterinario con id " + id);
        }
        try {
            service.delete(id);

            auditoria.registrar(
                    authHeader,
                    "Veterinario",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino el veterinario del empleado " + id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: el registro tiene referencias");
        }
    }
    @GetMapping("/me")
    public ResponseEntity<?> getMiPerfil(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token no válido o ausente."));
        }

        Usuario actor = sesionActual.usuario(authHeader); // Re-usa tu helper
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        Optional<Veterinario> vetOpt = service.findByUsuario(actor);
        if (vetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Perfil de veterinario no encontrado."));
        }

        return ResponseEntity.ok(vetOpt.get());
    }

}