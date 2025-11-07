package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import co.edu.unbosque.veterinaria.service.api.AplicacionVacunaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/aplicaciones-vacuna")
public class AplicacionVacunaRestController {

    @Autowired private AplicacionVacunaServiceAPI service;

    @GetMapping("/getAll")
    public List<AplicacionVacuna> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public AplicacionVacuna get(@PathVariable Long id) throws ResourceNotFoundException {
        AplicacionVacuna a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("Aplicación no encontrada: " + id);
        return a;
    }

    @PostMapping("/save")
    public AplicacionVacuna save(@RequestBody AplicacionVacuna a) { return service.save(a); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
