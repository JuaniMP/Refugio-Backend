package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Refugio;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.RefugioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/refugios")
public class RefugioRestController {

    @Autowired private AuditoriaRegistrador auditoria;

    @Autowired private RefugioServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    @GetMapping("/getAll")
    public List<Refugio> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Refugio> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Refugio r = service.get(id);
        if (r == null) throw new ResourceNotFoundException("refugio no encontrado: " + id);
        return ResponseEntity.ok(r);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Refugio r,
                                  @RequestHeader("Authorization") String authHeader) {
        boolean esNuevo = (r.getIdRefugio() == null);

        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre del refugio es obligatorio");
        }

        Refugio guardado;
        try {
            guardado = service.save(r);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: verifique restricciones de unicidad o datos asociados");
        }

        String comentario = esNuevo
                ? "se creó el refugio '" + guardado.getNombre() + "'"
                : "se actualizó el refugio '" + guardado.getNombre() + "' (id=" + guardado.getIdRefugio() + ")";

        auditoria.registrar(
                authHeader,
                "Refugio",
                guardado.getIdRefugio() != null ? guardado.getIdRefugio().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) {
        Refugio r = service.get(id);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe un refugio con id " + id);
        }

        try {
            service.delete(id);

            auditoria.registrar(
                    authHeader,
                    "Refugio",
                    id.toString(),
                    Accion.DELETE,
                    "se eliminó el refugio '" + r.getNombre() + "' (id=" + id + ")"
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: tiene datos asociados");
        }
    }

}