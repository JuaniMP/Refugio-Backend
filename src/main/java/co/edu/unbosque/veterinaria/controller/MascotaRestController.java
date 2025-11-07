package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.service.api.MascotaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/mascotas")
public class MascotaRestController {

    @Autowired private MascotaServiceAPI service;

    @GetMapping("/getAll")
    public List<Mascota> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Mascota get(@PathVariable Long id) throws ResourceNotFoundException {
        Mascota m = service.get(id);
        if (m == null) throw new ResourceNotFoundException("Mascota no encontrada: " + id);
        return m;
    }

    @PostMapping("/save")
    public Mascota save(@RequestBody Mascota m) { return service.save(m); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede eliminar: tiene historial/relaciones.");
        }
    }
}
