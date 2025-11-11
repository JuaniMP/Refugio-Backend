package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.service.api.AsignacionCuidadorServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionCuidadorRestController {

    @Autowired private AsignacionCuidadorServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar todas las asignaciones
    @GetMapping("/getAll")
    public List<AsignacionCuidador> getAll() {
        return service.getAll();
    }

    // crear nueva asignacion (no se permite update)
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody AsignacionCuidador a) {
        // validaciones básicas
        if (a.getIdMascota() == null || a.getIdEmpleado() == null || a.getFechaInicio() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar idMascota, idEmpleado y fechaInicio");
        }

        // validar si ya existe esa asignacion exacta
        AsignacionCuidadorId id = new AsignacionCuidadorId(a.getIdMascota(), a.getIdEmpleado(), a.getFechaInicio());
        AsignacionCuidador existente = service.get(id);
        if (existente != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("ya existe una asignacion con esos datos");
        }

        try {
            AsignacionCuidador guardada = service.save(a);

            registrarAuditoria(
                    "Asignacion_Cuidador",
                    id.toString(),
                    Accion.INSERT,
                    "se asigno el cuidador " + a.getIdEmpleado() + " a la mascota " + a.getIdMascota()
            );

            return ResponseEntity.ok(guardada);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar la asignacion (verifica relaciones o duplicados)");
        }
    }

    // eliminar asignacion (fin de la asignacion)
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Integer idMascota,
                                    @RequestParam Integer idEmpleado,
                                    @RequestParam String fechaInicio) {
        try {
            LocalDate fecha = LocalDate.parse(fechaInicio);
            AsignacionCuidadorId id = new AsignacionCuidadorId(idMascota, idEmpleado, fecha);
            AsignacionCuidador existente = service.get(id);

            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la asignacion indicada");
            }

            service.delete(id);

            registrarAuditoria(
                    "Asignacion_Cuidador",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la asignacion del cuidador " + idEmpleado + " a la mascota " + idMascota
            );

            return ResponseEntity.ok().build();

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("error al eliminar la asignacion: " + ex.getMessage());
        }
    }

    // helper para crear la auditoria
    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // cuando tengas autenticacion, agregas el usuario
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
