package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.service.api.CuidadorServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/cuidadores")
public class CuidadorRestController {

    @Autowired private CuidadorServiceAPI service;

    @GetMapping("/getAll")
    public List<Cuidador> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Cuidador get(@PathVariable Long id) throws ResourceNotFoundException {
        Cuidador c = service.get(id);
        if (c == null) throw new ResourceNotFoundException("Cuidador no encontrado: " + id);
        return c;
    }

    @PostMapping("/save")
    public Cuidador save(@RequestBody Cuidador c) { return service.save(c); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); } // quitar rol
}
