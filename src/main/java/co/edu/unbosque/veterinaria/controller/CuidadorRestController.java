package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.CuidadorServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
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
@RequestMapping("/api/cuidadores")
public class CuidadorRestController {

    @Autowired private CuidadorServiceAPI cuidadorService;
    @Autowired private EmpleadoServiceAPI empleadoService;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    @GetMapping("/getAll")
    public List<Cuidador> getAll() { return cuidadorService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Cuidador> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Cuidador c = cuidadorService.get(id);
        if (c == null) throw new ResourceNotFoundException("cuidador no encontrado: " + id);
        return ResponseEntity.ok(c);
    }

    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Cuidador c,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        // 1) Normalizar: extrae el id del empleado del JSON
        if (c.getEmpleado() == null || c.getEmpleado().getIdEmpleado() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el empleado con su id");
        }
        Integer idEmp = c.getEmpleado().getIdEmpleado();

        // 2) Traer Empleado gestionado (NO usar el objeto anidado del body)
        Empleado emp = empleadoService.get(idEmp);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("el empleado " + idEmp + " no existe");
        }

        // 3) Saber si es insert o update
        boolean existe = (cuidadorService.get(idEmp) != null);
        boolean esNuevo = !existe;

        if (esNuevo) {
            // INSERT con @MapsId: PK null y setear empleado gestionado
            c.setIdEmpleado(null);
        } else {
            // UPDATE: mantén la PK
            c.setIdEmpleado(idEmp);
        }

        // Reemplazar el empleado del body por el gestionado
        c.setEmpleado(emp);

        try {
            Cuidador guardado = cuidadorService.save(c);

            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Cuidador",
                    guardado.getIdEmpleado() != null ? guardado.getIdEmpleado().toString() : null,
                    esNuevo ? Accion.INSERT : Accion.UPDATE,
                    esNuevo
                            ? "se creó cuidador para empleado " + idEmp
                            : "se actualizó cuidador del empleado " + idEmp
            );

            return ResponseEntity.ok(guardado);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: restricción de integridad (FK inexistente o PK duplicada)");
        }
    }

    // --- 7. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        Cuidador c = cuidadorService.get(id);
        if (c == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe un cuidador con id_empleado " + id);
        }
        try {
            cuidadorService.delete(id);

            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Cuidador",
                    id.toString(),
                    Accion.DELETE,
                    "se eliminó el cuidador asociado al empleado " + id
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: el registro tiene referencias");
        }
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