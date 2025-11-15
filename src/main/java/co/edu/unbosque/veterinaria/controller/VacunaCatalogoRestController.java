package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.VacunaCatalogo;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VacunaCatalogoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects; // <-- AÑADIR IMPORT
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/vacunas")
public class VacunaCatalogoRestController {

    @Autowired private VacunaCatalogoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;
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

    // --- ⬇️ MÉTODO 'save' MODIFICADO ⬇️ ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody VacunaCatalogo v,
                                  @RequestHeader("Authorization") String authHeader) {
        boolean esNuevo = (v.getIdVacuna() == null);

        if (v.getNombre() == null || v.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");
        }

        // Lógica de estado (copiada de Especie/Raza)
        if (esNuevo) {
            if (v.getEstado() == null) {
                v.setEstado("ACTIVO");
            }
        } else {
            VacunaCatalogo existente = service.get(v.getIdVacuna());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la vacuna con id " + v.getIdVacuna());
            }
            // Si el request no trae estado, mantenemos el existente
            if (v.getEstado() == null) {
                v.setEstado(existente.getEstado());
            }
        }

        // Validación de duplicados (sin cambios)
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

            // ... (Lógica de auditoría sin cambios) ...
            registrarAuditoria(
                    authHeader,
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

    // ... (delete y registrarAuditoria sin cambios) ...
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) {
        VacunaCatalogo v = service.get(id);
        if (v == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la vacuna con id " + id);
        }

        try {
            service.delete(id);

            registrarAuditoria(
                    authHeader,
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

    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Accion accion, String comentario) {
        Usuario actor = null;
        try {
            String token = authHeader.substring(7);
            String login = jwtUtil.getLoginFromToken(token);
            Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
            if (usuarioOpt.isPresent()) {
                actor = usuarioOpt.get();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario para auditoría: " + e.getMessage());
        }

        Auditoria aud = Auditoria.builder()
                .usuario(actor)
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}