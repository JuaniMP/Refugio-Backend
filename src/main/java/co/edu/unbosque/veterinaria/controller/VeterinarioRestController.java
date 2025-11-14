package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
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
@RequestMapping("/api/veterinarios")
public class VeterinarioRestController {

    @Autowired private VeterinarioServiceAPI service;
    @Autowired private EmpleadoServiceAPI empleadoService;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
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

    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Veterinario body,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO

        // ... (validaciones existentes sin cambios) ...
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

            // --- 7. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
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


    // --- 8. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        Veterinario v = service.get(id);
        if (v == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no existe el veterinario con id " + id);
        }
        try {
            service.delete(id);

            // --- 9. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
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