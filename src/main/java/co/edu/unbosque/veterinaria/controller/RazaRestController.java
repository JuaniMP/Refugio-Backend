package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Raza;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.RazaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/razas")
public class RazaRestController {

    @Autowired private RazaServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar
    @GetMapping("/getAll")
    public List<Raza> getAll() {
        return service.getAll();
    }

    // obtener por id
    @GetMapping("/{id}")
    public ResponseEntity<Raza> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Raza r = service.get(id);
        if (r == null) throw new ResourceNotFoundException("raza no encontrada: " + id);
        return ResponseEntity.ok(r);
    }

    // crear o actualizar
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Raza r) {
        boolean esNuevo = (r.getIdRaza() == null);

        // validaciones basicas
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");
        }
        if (r.getEspecie() == null || r.getEspecie().getIdEspecie() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar la especie");
        }

        // si es update, validar existencia
        if (!esNuevo) {
            Raza existente = service.get(r.getIdRaza());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la raza con id " + r.getIdRaza());
            }
        }

        // validar duplicados por especie + nombre
        for (Raza otra : service.getAll()) {
            if (r.getNombre().equalsIgnoreCase(otra.getNombre())
                    && Objects.equals(r.getEspecie().getIdEspecie(), otra.getEspecie().getIdEspecie())) {
                if (esNuevo || !Objects.equals(r.getIdRaza(), otra.getIdRaza())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("ya existe una raza '" + r.getNombre() + "' para esa especie");
                }
            }
        }

        Raza guardada;
        try {
            guardada = service.save(r);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: revise las relaciones o duplicados");
        }

        registrarAuditoria(
                "Raza",
                guardada.getIdRaza() != null ? guardada.getIdRaza().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                esNuevo
                        ? "se creo raza '" + guardada.getNombre() + "' para la especie " + guardada.getEspecie().getIdEspecie()
                        : "se actualizo raza '" + guardada.getNombre() + "' (id=" + guardada.getIdRaza() + ")"
        );

        return ResponseEntity.ok(guardada);
    }

    // eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Raza r = service.get(id);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la raza con id " + id);
        }

        try {
            service.delete(id);
            registrarAuditoria(
                    "Raza",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la raza '" + r.getNombre() + "' (id=" + id + ")"
            );
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: esta en uso por mascotas");
        }
    }

    // helper auditoria
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
