package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoRestController {

    @Autowired private EmpleadoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar todos
    @GetMapping("/getAll")
    public List<Empleado> getAll() {
        return service.getAll();
    }

    // obtener por id (integer)
    @GetMapping("/{id}")
    public ResponseEntity<Empleado> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Empleado e = service.get(id);
        if (e == null) throw new ResourceNotFoundException("empleado no encontrado: " + id);
        return ResponseEntity.ok(e);
    }

    // crear o actualizar
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Empleado e) {
        boolean esNuevo = (e.getIdEmpleado() == null);

        // validaciones basicas
        if (e.getRefugio() == null || e.getRefugio().getIdRefugio() == null) { // <-- fix: getRefugio()
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el refugio (idRefugio)");
        }
        // si manejas empleado con usuario obligatorio, valida aqui:
        // if (e.getUsuario() == null || e.getUsuario().getIdUsuario() == null) { ... }

        // si es update, validar existencia
        if (!esNuevo) {
            Empleado existente = service.get(e.getIdEmpleado());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el empleado con id " + e.getIdEmpleado());
            }
        }

        // validar cedula unica si llega (nuevo o cambio en update)
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

        // normalizar nombre y telefono
        if (e.getNombre() != null) e.setNombre(e.getNombre().trim());
        if (e.getTelefono() != null) e.setTelefono(e.getTelefono().trim());

        // guardar
        Empleado guardado;
        try {
            guardado = service.save(e);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: posible cedula duplicada u otra restriccion");
        }

        // auditoria
        String comentario = esNuevo
                ? "se creo empleado " + safeNombre(guardado) + " id=" + guardado.getIdEmpleado()
                : "se actualizo empleado " + safeNombre(guardado) + " id=" + guardado.getIdEmpleado();

        registrarAuditoria(
                "Empleado",
                guardado.getIdEmpleado() != null ? guardado.getIdEmpleado().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Empleado e = service.get(id);
        if (e == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe el empleado con id " + id);
        }
        try {
            service.delete(id);

            registrarAuditoria(
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

    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // cuando haya autenticacion, setea usuario actor aqui
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
