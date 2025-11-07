package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import co.edu.unbosque.veterinaria.service.api.SolicitudAdopcionServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudAdopcionRestController {

    @Autowired private SolicitudAdopcionServiceAPI service;

    @GetMapping("/getAll")
    public List<SolicitudAdopcion> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public SolicitudAdopcion get(@PathVariable Long id) throws ResourceNotFoundException {
        SolicitudAdopcion s = service.get(id);
        if (s == null) throw new ResourceNotFoundException("Solicitud no encontrada: " + id);
        return s;
    }

    @PostMapping("/save")
    public SolicitudAdopcion save(@RequestBody SolicitudAdopcion s) { return service.save(s); }
}
