package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Especie;
import co.edu.unbosque.veterinaria.service.api.EspecieServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/especies")
public class EspecieRestController {

    @Autowired private EspecieServiceAPI service;

    @GetMapping("/getAll")
    public List<Especie> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Especie get(@PathVariable Long id) throws ResourceNotFoundException {
        Especie e = service.get(id);
        if (e == null) throw new ResourceNotFoundException("Especie no encontrada: " + id);
        return e;
    }

    @PostMapping("/save")
    public Especie save(@RequestBody Especie e) { return service.save(e); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede eliminar: está en uso.");
        }
    }
}
