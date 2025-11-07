package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import co.edu.unbosque.veterinaria.service.api.HistorialMedicoServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/historiales")
public class HistorialMedicoRestController {

    @Autowired private HistorialMedicoServiceAPI service;

    @GetMapping("/getAll")
    public List<HistorialMedico> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public HistorialMedico get(@PathVariable Long id) throws ResourceNotFoundException {
        HistorialMedico h = service.get(id);
        if (h == null) throw new ResourceNotFoundException("Historial no encontrado: " + id);
        return h;
    }

    @PostMapping("/save")
    public HistorialMedico save(@RequestBody HistorialMedico h) { return service.save(h); }
}
