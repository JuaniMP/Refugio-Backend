package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Especie;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EspecieServiceAPI;
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
@RequestMapping("/api/especies")
public class EspecieRestController {

    @Autowired private EspecieServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<Especie> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Especie> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Especie e = service.get(id);
        if (e == null) throw new ResourceNotFoundException("especie no encontrada: " + id);
        return ResponseEntity.ok(e);
    }

    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Especie e,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = (e.getIdEspecie() == null);

        // ... (validaciones de nombre y estado sin cambios) ...
        if (e.getNombre() == null || e.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");
        }
        e.setNombre(e.getNombre().trim());

        if (esNuevo) {
            if (e.getEstado() == null) {
                e.setEstado("ACTIVO");
            }
        } else {
            Especie existente = service.get(e.getIdEspecie());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la especie con id " + e.getIdEspecie());
            }
            if (e.getEstado() == null) {
                e.setEstado(existente.getEstado());
            }
        }

        for (Especie otra : service.getAll()) {
            if (e.getNombre().equalsIgnoreCase(otra.getNombre())) {
                if (esNuevo || !Objects.equals(otra.getIdEspecie(), e.getIdEspecie())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("ya existe una especie con nombre " + e.getNombre());
                }
            }
        }

        Especie guardada;
        try {
            guardada = service.save(e);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: posible nombre duplicado");
        }

        // --- 7. LLAMADA AL HELPER ACTUALIZADA ---
        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Especie",
                guardada.getIdEspecie() != null ? guardada.getIdEspecie().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                esNuevo
                        ? "se creo especie '" + guardada.getNombre() + "'"
                        : "se actualizo especie '" + guardada.getNombre() + "' (id=" + guardada.getIdEspecie() + ")"
        );

        return ResponseEntity.ok(guardada);
    }

    // --- 8. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        Especie e = service.get(id);
        if (e == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la especie con id " + id);
        }

        try {
            service.delete(id);

            // --- 9. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Especie",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la especie '" + e.getNombre() + "' (id=" + id + ")"
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: esta en uso");
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