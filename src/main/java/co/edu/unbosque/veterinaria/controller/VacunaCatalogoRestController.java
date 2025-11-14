package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.VacunaCatalogo;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VacunaCatalogoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/vacunas")
public class VacunaCatalogoRestController {

    @Autowired private VacunaCatalogoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<VacunaCatalogo> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<VacunaCatalogo> get(@PathVariable Integer id) throws ResourceNotFoundException {
        VacunaCatalogo v = service.get(id);
        if (v == null) throw new ResourceNotFoundException("vacuna no encontrada: " + id);
        return ResponseEntity.ok(v);
    }

    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody VacunaCatalogo v,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = (v.getIdVacuna() == null);

        // ... (validaciones existentes sin cambios) ...
        if (v.getNombre() == null || v.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");
        }
        if (!esNuevo) {
            VacunaCatalogo existente = service.get(v.getIdVacuna());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la vacuna con id " + v.getIdVacuna());
            }
        }
        for (VacunaCatalogo otra : service.getAll()) {
            if (otra.getNombre() != null && otra.getNombre().equalsIgnoreCase(v.getNombre())) {
                if (esNuevo || !otra.getIdVacuna().equals(v.getIdVacuna())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("ya existe una vacuna con nombre '" + v.getNombre() + "'");
                }
            }
        }

        try {
            VacunaCatalogo guardada = service.save(v);

            // --- 7. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Vacuna_Catalogo",
                    guardada.getIdVacuna() != null ? guardada.getIdVacuna().toString() : null,
                    esNuevo ? Accion.INSERT : Accion.UPDATE,
                    esNuevo
                            ? "se creo la vacuna '" + guardada.getNombre() + "'"
                            : "se actualizo la vacuna '" + guardada.getNombre() + "' (id=" + guardada.getIdVacuna() + ")"
            );

            return ResponseEntity.ok(guardada);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: nombre duplicado o restriccion en bd");
        }
    }

    // --- 8. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        VacunaCatalogo v = service.get(id);
        if (v == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la vacuna con id " + id);
        }

        try {
            service.delete(id);

            // --- 9. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Vacuna_Catalogo",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la vacuna '" + v.getNombre() + "' (id=" + id + ")"
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: ya fue aplicada");
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