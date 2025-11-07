package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auditorias")
public class AuditoriaRestController {

    @Autowired private AuditoriaServiceAPI service;

    @GetMapping("/getAll")
    public List<Auditoria> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Auditoria get(@PathVariable Long id) throws ResourceNotFoundException {
        Auditoria a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("Auditoría no encontrada: " + id);
        return a;
    }

    @PostMapping("/save")
    public Auditoria save(@RequestBody Auditoria a) { return service.save(a); }

    // Sin delete
}
