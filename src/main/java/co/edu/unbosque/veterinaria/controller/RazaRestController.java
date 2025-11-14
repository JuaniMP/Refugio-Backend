package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Raza;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.RazaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/razas")
public class RazaRestController {

    @Autowired private RazaServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<Raza> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Raza> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Raza r = service.get(id);
        if (r == null) throw new ResourceNotFoundException("raza no encontrada: " + id);
        return ResponseEntity.ok(r);
    }


    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Raza r,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = (r.getIdRaza() == null);

        // ... (validaciones existentes sin cambios) ...
        if (r.getNombre() == null || r.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");
        }
        if (r.getEspecie() == null || r.getEspecie().getIdEspecie() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar la especie");
        }
        if (esNuevo) {
            if (r.getEstado() == null) {
                r.setEstado("ACTIVO");
            }
        } else {
            Raza existente = service.get(r.getIdRaza());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la raza con id " + r.getIdRaza());
            }
            if (r.getEstado() == null) {
                r.setEstado(existente.getEstado());
            }
        }
        for (Raza otra : service.getAll()) {
            if (r.getNombre().equalsIgnoreCase(otra.getNombre())
                    && Objects.equals(r.getEspecie().getIdEspecie(), otra.getEspecie().getIdEspecie())) {
                if (esNuevo || !Objects.equals(r.getIdRaza(), otra.getIdRaza())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("ya existe una raza '" + r.getNombre() + "' para esa especie");
                }
            }
        }

        Raza guardada;
        try {
            guardada = service.save(r);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: revise las relaciones o duplicados");
        }

        // --- 7. LLAMADA AL HELPER ACTUALIZADA ---
        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Raza",
                guardada.getIdRaza() != null ? guardada.getIdRaza().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                esNuevo
                        ? "se creo raza '" + guardada.getNombre() + "' para la especie " + guardada.getEspecie().getIdEspecie()
                        : "se actualizo raza '" + guardada.getNombre() + "' (id=" + guardada.getIdRaza() + ")"
        );

        return ResponseEntity.ok(guardada);
    }

    // --- 8. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        Raza r = service.get(id);
        if (r == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la raza con id " + id);
        }
        try {
            service.delete(id);
            // --- 9. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Raza",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la raza '" + r.getNombre() + "' (id=" + id + ")"
            );
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: esta en uso por mascotas");
        }
    }

    // --- 10. HELPER DE AUDITORÍA ACTUALIZADO ---
    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Accion accion, String comentario) {
        Usuario actor = null;
        try {
            // Extraer el usuario del token
            String token = authHeader.substring(7); // Quita "Bearer "
            String login = jwtUtil.getLoginFromToken(token);
            Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
            if (usuarioOpt.isPresent()) {
                actor = usuarioOpt.get();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario para auditoría: " + e.getMessage());
        }

        Auditoria aud = Auditoria.builder()
                .usuario(actor) // <-- Se asigna el actor (o null si falló)
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
