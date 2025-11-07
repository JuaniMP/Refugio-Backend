package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.TelefonoRefugio;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugioId;
import co.edu.unbosque.veterinaria.service.api.TelefonoRefugioServiceAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/telefonos-refugio")
public class TelefonoRefugioRestController {

    @Autowired private TelefonoRefugioServiceAPI service;

    @GetMapping("/getAll")
    public List<TelefonoRefugio> getAll() { return service.getAll(); }

    @PostMapping("/save")
    public TelefonoRefugio save(@RequestBody TelefonoRefugio t) { return service.save(t); }

    @DeleteMapping
    public void delete(@RequestParam Long idRefugio, @RequestParam String telefono) {
        service.delete(new TelefonoRefugioId(idRefugio, telefono));
    }
}
