package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Refugio;
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

    @GetMapping("/getAll")
    public List<Refugio> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Refugio get(@PathVariable Long id) throws ResourceNotFoundException {
        Refugio r = service.get(id);
        if (r == null) throw new ResourceNotFoundException("Refugio no encontrado: " + id);
        return r;
    }

    @PostMapping("/save")
    public Refugio save(@RequestBody Refugio r) { return service.save(r); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede eliminar: tiene datos asociados.");
        }
    }
}
