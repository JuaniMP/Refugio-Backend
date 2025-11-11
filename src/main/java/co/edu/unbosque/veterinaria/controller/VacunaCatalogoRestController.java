package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.VacunaCatalogo;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VacunaCatalogoServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/vacunas")
public class VacunaCatalogoRestController {

    @Autowired private VacunaCatalogoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar todo
    @GetMapping("/getAll")
    public List<VacunaCatalogo> getAll() {
        return service.getAll();
    }

    // obtener por id
    @GetMapping("/{id}")
    public ResponseEntity<VacunaCatalogo> get(@PathVariable Integer id) throws ResourceNotFoundException {
        VacunaCatalogo v = service.get(id);
        if (v == null) throw new ResourceNotFoundException("vacuna no encontrada: " + id);
        return ResponseEntity.ok(v);
    }

    // crear o actualizar
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody VacunaCatalogo v) {
        boolean esNuevo = (v.getIdVacuna() == null);

        // validacion basica
        if (v.getNombre() == null || v.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");
        }

        // si es update, validar existencia
        if (!esNuevo) {
            VacunaCatalogo existente = service.get(v.getIdVacuna());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la vacuna con id " + v.getIdVacuna());
            }
        }

        // verificar duplicado por nombre (case-insensitive)
        for (VacunaCatalogo otra : service.getAll()) {
            if (otra.getNombre() != null && otra.getNombre().equalsIgnoreCase(v.getNombre())) {
                if (esNuevo || !otra.getIdVacuna().equals(v.getIdVacuna())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("ya existe una vacuna con nombre '" + v.getNombre() + "'");
                }
            }
        }

        try {
            VacunaCatalogo guardada = service.save(v);

            registrarAuditoria(
                    "Vacuna_Catalogo",
                    guardada.getIdVacuna() != null ? guardada.getIdVacuna().toString() : null,
                    esNuevo ? Accion.INSERT : Accion.UPDATE,
                    esNuevo
                            ? "se creo la vacuna '" + guardada.getNombre() + "'"
                            : "se actualizo la vacuna '" + guardada.getNombre() + "' (id=" + guardada.getIdVacuna() + ")"
            );

            return ResponseEntity.ok(guardada);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: nombre duplicado o restriccion en bd");
        }
    }

    // eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        VacunaCatalogo v = service.get(id);
        if (v == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la vacuna con id " + id);
        }

        try {
            service.delete(id);

            registrarAuditoria(
                    "Vacuna_Catalogo",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la vacuna '" + v.getNombre() + "' (id=" + id + ")"
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: ya fue aplicada");
        }
    }

    // helper auditoria
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
