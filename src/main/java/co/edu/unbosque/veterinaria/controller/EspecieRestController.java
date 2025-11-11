package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Especie;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EspecieServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/especies")
public class EspecieRestController {

    @Autowired private EspecieServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar
    @GetMapping("/getAll")
    public List<Especie> getAll() {
        return service.getAll();
    }

    // obtener por id (integer)
    @GetMapping("/{id}")
    public ResponseEntity<Especie> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Especie e = service.get(id);
        if (e == null) throw new ResourceNotFoundException("especie no encontrada: " + id);
        return ResponseEntity.ok(e);
    }

    // crear o actualizar
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Especie e) {
        boolean esNuevo = (e.getIdEspecie() == null);

        // validacion nombre obligatorio
        if (e.getNombre() == null || e.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");
        }
        e.setNombre(e.getNombre().trim());

        // si es update, validar existencia
        if (!esNuevo) {
            Especie existente = service.get(e.getIdEspecie());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la especie con id " + e.getIdEspecie());
            }
        }

        // validar nombre unico (nuevo o cambio de nombre)
        for (Especie otra : service.getAll()) {
            if (e.getNombre().equalsIgnoreCase(otra.getNombre())) {
                if (esNuevo || !Objects.equals(otra.getIdEspecie(), e.getIdEspecie())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("ya existe una especie con nombre " + e.getNombre());
                }
            }
        }

        // guardar
        Especie guardada;
        try {
            guardada = service.save(e);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: posible nombre duplicado");
        }

        // auditoria
        registrarAuditoria(
                "Especie",
                guardada.getIdEspecie() != null ? guardada.getIdEspecie().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                esNuevo
                        ? "se creo especie '" + guardada.getNombre() + "'"
                        : "se actualizo especie '" + guardada.getNombre() + "' (id=" + guardada.getIdEspecie() + ")"
        );

        return ResponseEntity.ok(guardada);
    }

    // eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Especie e = service.get(id);
        if (e == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la especie con id " + id);
        }

        try {
            service.delete(id);

            registrarAuditoria(
                    "Especie",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la especie '" + e.getNombre() + "' (id=" + id + ")"
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: esta en uso");
        }
    }

    // helper auditoria
    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // cuando tengas autenticacion, setea el actor aqui
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
