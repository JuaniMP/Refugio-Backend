package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioRestController {

    @Autowired private VeterinarioServiceAPI service;
    @Autowired private EmpleadoServiceAPI empleadoService;   // <-- ADJUNTAR EMPLEADO
    @Autowired private AuditoriaServiceAPI auditoriaService;

    @GetMapping("/getAll")
    public List<Veterinario> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Veterinario> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Veterinario v = service.get(id);
        if (v == null) throw new ResourceNotFoundException("veterinario no encontrado: " + id);
        return ResponseEntity.ok(v);
    }


    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Veterinario body) {

        // 1) Validación: debe venir id del empleado
        if (body.getEmpleado() == null || body.getEmpleado().getIdEmpleado() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el empleado con su id");
        }
        Integer empId = body.getEmpleado().getIdEmpleado();

        // 2) Cargar entidades MANAGED
        Empleado emp = empleadoService.get(empId);
        if (emp == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe el empleado con id " + empId);
        }

        // ¿Ya existe un veterinario para ese empleado?
        Veterinario existente = service.get(empId);
        final boolean esNuevo = (existente == null);

        Veterinario target;
        if (esNuevo) {
            // 3a) INSERT: crear NUEVO sin id; @MapsId copiará emp.id -> id_empleado
            target = new Veterinario();
            target.setEmpleado(emp);
        } else {
            // 3b) UPDATE: usar la instancia MANAGED existente
            target = existente;
        }

        // Normalizar/actualizar campos
        if (body.getEspecialidad() != null)
            target.setEspecialidad(body.getEspecialidad().trim());
        if (body.getRegistroProfesional() != null)
            target.setRegistroProfesional(body.getRegistroProfesional().trim());

        try {
            Veterinario guardado = service.save(target);

            registrarAuditoria(
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
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Veterinario v = service.get(id);
        if (v == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no existe el veterinario con id " + id);
        }
        try {
            service.delete(id);
            registrarAuditoria("Veterinario", id.toString(), Accion.DELETE,
                    "se elimino el veterinario del empleado " + id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: el registro tiene referencias");
        }
    }

    private void registrarAuditoria(String tabla, String idRegistro, Auditoria.Accion accion, String comentario) {
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
