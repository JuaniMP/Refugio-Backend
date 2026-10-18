package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.CuidadorServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cuidadores")
public class CuidadorRestController {

    @Autowired private AuditoriaRegistrador auditoria;

    @Autowired private CuidadorServiceAPI cuidadorService;
    @Autowired private EmpleadoServiceAPI empleadoService;
    @Autowired private AuditoriaServiceAPI auditoriaService;

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

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Cuidador c,
                                  @RequestHeader("Authorization") String authHeader) {
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

            auditoria.registrar(
                    authHeader,
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) {
        Cuidador c = cuidadorService.get(id);
        if (c == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe un cuidador con id_empleado " + id);
        }
        try {
            cuidadorService.delete(id);

            auditoria.registrar(
                    authHeader,
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
    @GetMapping("/me")
    public ResponseEntity<?> getMiPerfil(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token no válido o ausente.");
        }

        String token = authHeader.substring(7);
        String login;
        try {
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
            }
            login = jwtUtil.getLoginFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error al procesar token.");
        }

        Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        // Usamos el nuevo método del servicio
        Optional<Cuidador> cuidadorOpt = cuidadorService.findByUsuario(usuarioOpt.get());
        if (cuidadorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Perfil de cuidador no encontrado.");
        }

        return ResponseEntity.ok(cuidadorOpt.get());
    }

}