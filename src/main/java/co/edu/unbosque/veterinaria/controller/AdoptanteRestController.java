package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Adoptante;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.service.api.AdoptanteServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/adoptantes")
public class AdoptanteRestController {

    @Autowired private AdoptanteServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // obtener todos
    @GetMapping("/getAll")
    public List<Adoptante> getAll() {
        return service.getAll();
    }

    // obtener por id
    @GetMapping("/{id}")
    public Adoptante get(@PathVariable Integer id) throws ResourceNotFoundException {
        Adoptante a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("adoptante no encontrado: " + id);
        return a;
    }

    // crear o actualizar
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Adoptante a) {
        boolean esNuevo = (a.getIdAdoptante() == null);
        Adoptante existente = null;

        // si viene con id, buscamos el registro
        if (!esNuevo) {
            existente = service.get(a.getIdAdoptante());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el adoptante con id " + a.getIdAdoptante());
            }
        }

        // validamos documento duplicado (si cambia o es nuevo)
        if (a.getDocumento() != null) {
            for (Adoptante otro : service.getAll()) {
                if (a.getDocumento().equalsIgnoreCase(otro.getDocumento())) {
                    if (esNuevo || !Objects.equals(otro.getIdAdoptante(), a.getIdAdoptante())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("ya existe un adoptante con el documento " + a.getDocumento());
                    }
                }
            }
        }

        // guardar adoptante
        Adoptante guardado = service.save(a);

        // auditoría
        String comentario = esNuevo
                ? "se registró un nuevo adoptante: " + guardado.getNombre()
                : "se actualizó la información del adoptante: " + guardado.getNombre();

        registrarAuditoria(
                "Adoptante",
                guardado.getIdAdoptante() != null ? guardado.getIdAdoptante().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // eliminar adoptante
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            Adoptante a = service.get(id);
            if (a == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el adoptante con id " + id);
            }

            service.delete(id);

            registrarAuditoria(
                    "Adoptante",
                    id.toString(),
                    Accion.DELETE,
                    "se eliminó el adoptante con id " + id + " (" + a.getNombre() + ")"
            );

            return ResponseEntity.ok().build();

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: tiene solicitudes o adopciones asociadas");
        }
    }

    // helper para crear fila de auditoría
    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // si luego usas usuario autenticado lo metes aca
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
