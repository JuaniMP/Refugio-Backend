package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AsignacionCuidadorServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionCuidadorRestController {

    @Autowired private AsignacionCuidadorServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;


    // ... (getAll sin cambios) ...
    @GetMapping("/getAll")
    public List<AsignacionCuidador> getAll() {
        return service.getAll();
    }


    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody AsignacionCuidador a,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO

        // ... (validaciones existentes sin cambios) ...
        if (a.getIdMascota() == null || a.getIdEmpleado() == null || a.getFechaInicio() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar idMascota, idEmpleado y fechaInicio");
        }
        AsignacionCuidadorId id = new AsignacionCuidadorId(a.getIdMascota(), a.getIdEmpleado(), a.getFechaInicio());
        AsignacionCuidador existente = service.get(id);
        if (existente != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("ya existe una asignacion con esos datos");
        }

        try {
            AsignacionCuidador guardada = service.save(a);

            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Asignacion_Cuidador",
                    id.toString(),
                    Accion.INSERT,
                    "se asigno el cuidador " + a.getIdEmpleado() + " a la mascota " + a.getIdMascota()
            );

            return ResponseEntity.ok(guardada);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar la asignacion (verifica relaciones o duplicados)");
        }
    }

    // --- 7. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Integer idMascota,
                                    @RequestParam Integer idEmpleado,
                                    @RequestParam String fechaInicio,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        try {
            LocalDate fecha = LocalDate.parse(fechaInicio);
            AsignacionCuidadorId id = new AsignacionCuidadorId(idMascota, idEmpleado, fecha);
            AsignacionCuidador existente = service.get(id);

            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe la asignacion indicada");
            }

            service.delete(id);

            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Asignacion_Cuidador",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la asignacion del cuidador " + idEmpleado + " a la mascota " + idMascota
            );

            return ResponseEntity.ok().build();

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("error al eliminar la asignacion: " + ex.getMessage());
        }
    }

    // --- 8. HELPER DE AUDITORÍA ACTUALIZADO ---
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