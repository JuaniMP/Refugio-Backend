package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AplicacionVacunaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/aplicaciones-vacuna")
public class AplicacionVacunaRestController {

    @Autowired private AplicacionVacunaServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;


    // ... (getAll y get sin cambios) ...
    @GetMapping("/getAll")
    public List<AplicacionVacuna> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public AplicacionVacuna get(@PathVariable Integer id) throws ResourceNotFoundException {
        AplicacionVacuna a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("aplicacion de vacuna no encontrada: " + id);
        return a;
    }


    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody AplicacionVacuna a,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO

        // ... (validaciones existentes sin cambios) ...
        if (a.getIdAplicacion() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("no se permite actualizar una aplicacion de vacuna existente; no debes enviar idAplicacion");
        }
        if (a.getHistorial() == null || a.getHistorial().getIdHistorial() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el historial medico (idHistorial)");
        }
        if (a.getVacuna() == null || a.getVacuna().getIdVacuna() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar la vacuna aplicada (idVacuna)");
        }
        if (a.getFecha() == null) {
            a.setFecha(LocalDate.now());
        }

        AplicacionVacuna guardada = service.save(a);

        // registrar auditoría
        registrarAuditoria(
                authHeader, // <-- AÑADIDO
                "Aplicacion_Vacuna",
                guardada.getIdAplicacion().toString(),
                Accion.INSERT,
                "se registro una nueva aplicacion de vacuna " +
                        guardada.getVacuna().getIdVacuna() +
                        " en historial " + guardada.getHistorial().getIdHistorial()
        );

        return ResponseEntity.ok(guardada);
    }

    // --- 7. MÉTODO 'delete' ACTUALIZADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        AplicacionVacuna a = service.get(id);
        if (a == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la aplicacion de vacuna con id " + id);
        }

        try {
            service.delete(id);
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Aplicacion_Vacuna",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la aplicacion de vacuna con id " + id
            );
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: esta vinculada a otros registros");
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
