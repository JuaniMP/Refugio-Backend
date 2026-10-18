package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Adopcion;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.repository.AdopcionRepository;
import co.edu.unbosque.veterinaria.service.api.AdopcionServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/adopciones")
public class AdopcionRestController {

    @Autowired private AuditoriaRegistrador auditoria;

    @Autowired private AdopcionServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private AdopcionRepository adopcionRepository;

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    @GetMapping("/getAll")
    public List<Adopcion> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public Adopcion get(@PathVariable Integer id) throws ResourceNotFoundException {
        Adopcion a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("adopcion no encontrada: " + id);
        return a;
    }

    // Ahora recibe el @RequestHeader("Authorization")
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Adopcion a,
                                  @RequestHeader("Authorization") String authHeader) {

        if (a.getIdAdopcion() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("no se permite actualizar adopciones; no debes enviar idAdopcion");
        }
        if (a.getMascota() == null || a.getMascota().getIdMascota() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar la mascota con idMascota");
        }
        if (a.getAdoptante() == null || a.getAdoptante().getIdAdoptante() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el adoptante con idAdoptante");
        }
        if (a.getFechaAdopcion() == null) {
            a.setFechaAdopcion(LocalDate.now());
        }
        Integer idMascota = a.getMascota().getIdMascota();
        if (adopcionRepository.existsByMascota_IdMascota(idMascota)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("la mascota " + idMascota + " ya fue adoptada");
        }
        Adopcion guardada = service.save(a);

        // Ahora pasamos el authHeader
        auditoria.registrar(
                authHeader,
                "Adopcion",
                guardada.getIdAdopcion() != null ? guardada.getIdAdopcion().toString() : null,
                Accion.INSERT,
                "se creo la adopcion de la mascota " + idMascota + " para el adoptante " + a.getAdoptante().getIdAdoptante()
        );

        return ResponseEntity.ok(guardada);
    }

}