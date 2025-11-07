package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Diagnostico;
import co.edu.unbosque.veterinaria.service.api.DiagnosticoServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoRestController {

    @Autowired private DiagnosticoServiceAPI service;

    @GetMapping("/getAll")
    public List<Diagnostico> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Diagnostico get(@PathVariable Long id) throws ResourceNotFoundException {
        Diagnostico d = service.get(id);
        if (d == null) throw new ResourceNotFoundException("Diagnóstico no encontrado: " + id);
        return d;
    }

    @PostMapping("/save")
    public Diagnostico save(@RequestBody Diagnostico d) { return service.save(d); }
}
