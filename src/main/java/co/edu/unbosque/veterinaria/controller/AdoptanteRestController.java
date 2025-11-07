package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Adoptante;
import co.edu.unbosque.veterinaria.service.api.AdoptanteServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/adoptantes")
public class AdoptanteRestController {

    @Autowired private AdoptanteServiceAPI service;

    @GetMapping("/getAll")
    public List<Adoptante> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Adoptante get(@PathVariable Long id) throws ResourceNotFoundException {
        Adoptante a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("Adoptante no encontrado: " + id);
        return a;
    }

    @PostMapping("/save")
    public Adoptante save(@RequestBody Adoptante a) { return service.save(a); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: tiene solicitudes/adopciones.");
        }
    }
}
