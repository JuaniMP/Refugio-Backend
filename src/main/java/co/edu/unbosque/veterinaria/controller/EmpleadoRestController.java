package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoRestController {

    @Autowired private EmpleadoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<Empleado> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Empleado> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Empleado e = service.get(id);
        if (e == null) throw new ResourceNotFoundException("empleado no encontrado: " + id);
        return ResponseEntity.ok(e);
    }

    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Empleado e,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = (e.getIdEmpleado() == null);

        // ... (validaciones existentes sin cambios) ...
        if (e.getRefugio() == null || e.getRefugio().getIdRefugio() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el refugio (idRefugio)");
        }
        if (!esNuevo) {
            Empleado existente = service.get(e.getIdEmpleado());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el empleado con id " + e.getIdEmpleado());
            }
        }
        if (e.getCedula() != null && !e.getCedula().isBlank()) {
            String cedulaNorm = e.getCedula().trim();
            for (Empleado otro : service.getAll()) {
                if (otro.getCedula() != null && cedulaNorm.equalsIgnoreCase(otro.getCedula().trim())) {
                    if (esNuevo || !Objects.equals(otro.getIdEmpleado(), e.getIdEmpleado())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("ya existe un empleado con la cedula " + cedulaNorm);
                    }
                }
            }
            e.setCedula(cedulaNorm);
        }
        if (e.getNombre() != null) e.setNombre(e.getNombre().trim());
        if (e.getTelefono() != null) e.setTelefono(e.getTelefono().trim());

        Empleado guardado;
        try {
            guardado = service.save(e);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: posible cedula duplicada u otra restriccion");
        }

        String comentario = esNuevo
                ? "se creo empleado " + safeNombre(guardado) + " id=" + guardado.getIdEmpleado()
                : "se actualizo empleado " + safeNombre(guardado) + " id=" + guardado.getIdEmpleado();

        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Empleado",
                guardado.getIdEmpleado() != null ? guardado.getIdEmpleado().toString() : null,
                !esNuevo ? Accion.UPDATE : Accion.INSERT,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // --- 7. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        Empleado e = service.get(id);
        if (e == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe el empleado con id " + id);
        }
        try {
            service.delete(id);

            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Empleado",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino el empleado " + safeNombre(e) + " id=" + id
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: tiene subroles o referencias");
        }
    }

    // ================= helpers =================

    private String safeNombre(Empleado e) {
        return (e.getNombre() == null || e.getNombre().isBlank()) ? "(sin nombre)" : e.getNombre();
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
