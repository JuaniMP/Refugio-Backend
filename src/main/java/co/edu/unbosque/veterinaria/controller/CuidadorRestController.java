package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.CuidadorServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/cuidadores")
public class CuidadorRestController {

    @Autowired private CuidadorServiceAPI cuidadorService;
    @Autowired private EmpleadoServiceAPI empleadoService;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    @GetMapping("/getAll")
    public List<Cuidador> getAll() { return cuidadorService.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Cuidador> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Cuidador c = cuidadorService.get(id);
        if (c == null) throw new ResourceNotFoundException("cuidador no encontrado: " + id);
        return ResponseEntity.ok(c);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Cuidador c) {
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
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Cuidador c = cuidadorService.get(id);
        if (c == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe un cuidador con id_empleado " + id);
        }
        try {
            cuidadorService.delete(id);

            registrarAuditoria(
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

    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null)
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
