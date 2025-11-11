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

    @Autowired
    private AuditoriaServiceAPI service;

    // listar todas las auditorias
    @GetMapping("/getAll")
    public List<Auditoria> getAll() {
        return service.getAll();
    }

    // obtener una auditoria por id
    @GetMapping("/{id}")
    public Auditoria get(@PathVariable Integer id) throws ResourceNotFoundException {
        Auditoria a = service.get(id);
        if (a == null) {
            throw new ResourceNotFoundException("auditoria no encontrada: " + id);
        }
        return a;
    }

    // opcional: permitir crear auditorias manuales
    @PostMapping("/save")
    public Auditoria save(@RequestBody Auditoria a) {
        return service.save(a);
    }

    // no hay delete, las auditorias no se borran
}
