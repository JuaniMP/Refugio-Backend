package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Refugio;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.RefugioServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/refugios")
public class RefugioRestController {

    @Autowired private RefugioServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar todos
    @GetMapping("/getAll")
    public List<Refugio> getAll() {
        return service.getAll();
    }

    // obtener por id
    @GetMapping("/{id}")
    public ResponseEntity<Refugio> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Refugio r = service.get(id);
        if (r == null) throw new ResourceNotFoundException("refugio no encontrado: " + id);
        return ResponseEntity.ok(r);
    }

    // crear o actualizar refugio
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Refugio r) {
        boolean esNuevo = (r.getIdRefugio() == null);

        // validaciones mínimas
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre del refugio es obligatorio");
        }

        Refugio guardado;
        try {
            guardado = service.save(r);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: verifique restricciones de unicidad o datos asociados");
        }

        // comentario para auditoría
        String comentario = esNuevo
                ? "se creó el refugio '" + guardado.getNombre() + "'"
                : "se actualizó el refugio '" + guardado.getNombre() + "' (id=" + guardado.getIdRefugio() + ")";

        registrarAuditoria(
                "Refugio",
                guardado.getIdRefugio() != null ? guardado.getIdRefugio().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // eliminar refugio
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Refugio r = service.get(id);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe un refugio con id " + id);
        }

        try {
            service.delete(id);

            registrarAuditoria(
                    "Refugio",
                    id.toString(),
                    Accion.DELETE,
                    "se eliminó el refugio '" + r.getNombre() + "' (id=" + id + ")"
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: tiene datos asociados");
        }
    }

    // ===== helper auditoría =====
    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // cuando se maneje login, se asocia el actor
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
