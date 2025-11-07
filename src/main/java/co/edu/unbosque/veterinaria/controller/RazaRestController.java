package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Raza;
import co.edu.unbosque.veterinaria.service.api.RazaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/razas")
public class RazaRestController {

    @Autowired private RazaServiceAPI service;

    @GetMapping("/getAll")
    public List<Raza> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Raza get(@PathVariable Long id) throws ResourceNotFoundException {
        Raza r = service.get(id);
        if (r == null) throw new ResourceNotFoundException("Raza no encontrada: " + id);
        return r;
    }

    @PostMapping("/save")
    public Raza save(@RequestBody Raza r) { return service.save(r); }

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
