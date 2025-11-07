package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import co.edu.unbosque.veterinaria.service.api.AsignacionCuidadorServiceAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionCuidadorRestController {

    @Autowired private AsignacionCuidadorServiceAPI service;

    @GetMapping("/getAll")
    public List<AsignacionCuidador> getAll() { return service.getAll(); }

    @PostMapping("/save")
    public AsignacionCuidador save(@RequestBody AsignacionCuidador a) { return service.save(a); }

    @DeleteMapping
    public void delete(@RequestParam Long idMascota,
                       @RequestParam Long idEmpleado,
                       @RequestParam String fechaInicio) {
        service.delete(new AsignacionCuidadorId(idMascota, idEmpleado, LocalDate.parse(fechaInicio)));
    }
}
