package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.VacunaCatalogo;
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

    @GetMapping("/getAll")
    public List<VacunaCatalogo> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public VacunaCatalogo get(@PathVariable Long id) throws ResourceNotFoundException {
        VacunaCatalogo v = service.get(id);
        if (v == null) throw new ResourceNotFoundException("Vacuna no encontrada: " + id);
        return v;
    }

    @PostMapping("/save")
    public VacunaCatalogo save(@RequestBody VacunaCatalogo v) { return service.save(v); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede eliminar: ya fue aplicada.");
        }
    }
}
