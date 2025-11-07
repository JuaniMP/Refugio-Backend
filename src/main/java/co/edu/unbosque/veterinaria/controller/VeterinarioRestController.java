package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioRestController {

    @Autowired private VeterinarioServiceAPI service;

    @GetMapping("/getAll")
    public List<Veterinario> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Veterinario get(@PathVariable Long id) throws ResourceNotFoundException {
        Veterinario v = service.get(id);
        if (v == null) throw new ResourceNotFoundException("Veterinario no encontrado: " + id);
        return v;
    }

    @PostMapping("/save")
    public Veterinario save(@RequestBody Veterinario v) { return service.save(v); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); } // quitar rol
}
