package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Adopcion;
import co.edu.unbosque.veterinaria.repository.AdopcionRepository;
import co.edu.unbosque.veterinaria.service.api.AdopcionServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/adopciones")
public class AdopcionRestController {

    @Autowired private AdopcionServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private AdopcionRepository adopcionRepository;

    // listar todas las adopciones
    @GetMapping("/getAll")
    public List<Adopcion> getAll() {
        return service.getAll();
    }

    // obtener una adopcion por id
    @GetMapping("/{id}")
    public Adopcion get(@PathVariable Integer id) throws ResourceNotFoundException {
        Adopcion a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("adopcion no encontrada: " + id);
        return a;
    }

    // crear adopcion (inmutable: no se permiten updates)
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Adopcion a) {
        // si viene con id, rechazamos porque adopcion es inmutable
        if (a.getIdAdopcion() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("no se permite actualizar adopciones; no debes enviar idAdopcion");
        }

        // validaciones basicas
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

        // evitar doble adopcion de la misma mascota (la columna ya es unique, pero validamos antes)
        Integer idMascota = a.getMascota().getIdMascota();
        if (adopcionRepository.existsByMascota_IdMascota(idMascota)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("la mascota " + idMascota + " ya fue adoptada");
        }

        // guardar
        Adopcion guardada = service.save(a);

        // auditoria: insert
        registrarAuditoria(
                "Adopcion",
                guardada.getIdAdopcion() != null ? guardada.getIdAdopcion().toString() : null,
                Accion.INSERT,
                "se creo la adopcion de la mascota " + idMascota + " para el adoptante " + a.getAdoptante().getIdAdoptante()
        );

        return ResponseEntity.ok(guardada);
    }

    // helper auditoria simple con comentario
    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // si luego tienes el usuario actor autenticado, lo pones aqui
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
