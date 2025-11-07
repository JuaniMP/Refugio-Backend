package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoRestController {

    @Autowired private EmpleadoServiceAPI service;

    @GetMapping("/getAll")
    public List<Empleado> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Empleado get(@PathVariable Long id) throws ResourceNotFoundException {
        Empleado e = service.get(id);
        if (e == null) throw new ResourceNotFoundException("Empleado no encontrado: " + id);
        return e;
    }

    @PostMapping("/save")
    public Empleado save(@RequestBody Empleado e) { return service.save(e); }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: tiene subroles o referencias.");
        }
    }
}
