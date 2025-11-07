package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Adopcion;
import co.edu.unbosque.veterinaria.service.api.AdopcionServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/adopciones")
public class AdopcionRestController {

    @Autowired private AdopcionServiceAPI service;

    @GetMapping("/getAll")
    public List<Adopcion> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Adopcion get(@PathVariable Long id) throws ResourceNotFoundException {
        Adopcion a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("Adopción no encontrada: " + id);
        return a;
    }

    @PostMapping("/save")
    public Adopcion save(@RequestBody Adopcion a) { return service.save(a); }
}
