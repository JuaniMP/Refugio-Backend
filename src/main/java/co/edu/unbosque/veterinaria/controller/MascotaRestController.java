package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- 1. IMPORTAR
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.MascotaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- 2. IMPORTAR
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- 3. IMPORTAR
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unbosque.veterinaria.entity.Veterinario; // <-- AÑADIR
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI; // <-- AÑADIR
import org.springframework.transaction.annotation.Transactional;import co.edu.unbosque.veterinaria.entity.Veterinario; // <-- AÑADIR
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI; // <-- AÑADIR
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional; // <-- 4. IMPORTAR

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/mascotas")
public class MascotaRestController {

    @Autowired private MascotaServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- 5. DEPENDENCIAS AÑADIDAS ---
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    @Autowired private VeterinarioServiceAPI veterinarioService;

    @GetMapping("/getAll")
    public List<Mascota> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Mascota m = service.get(id);
        if (m == null) throw new ResourceNotFoundException("mascota no encontrada: " + id);
        return ResponseEntity.ok(m);
    }

    // --- 6. MÉTODO 'save' ACTUALIZADO ---
    @Transactional
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Mascota m,
                                  @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        boolean esNuevo = (m.getIdMascota() == null);

        // Validaciones que sí existen en la tabla
        if (m.getNombre() == null || m.getNombre().isBlank())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");

        if (m.getRaza() == null || m.getRaza().getIdRaza() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar la raza (idRaza)");

        if (m.getRefugio() == null || m.getRefugio().getIdRefugio() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar el refugio (idRefugio)");

        try {
            Mascota guardada = service.save(m);

            // --- 7. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Mascota",
                    guardada.getIdMascota() != null ? guardada.getIdMascota().toString() : null,
                    esNuevo ? Auditoria.Accion.INSERT : Auditoria.Accion.UPDATE,
                    (esNuevo ? "se registro" : "se actualizo") + " la mascota '" + guardada.getNombre() + "'"
            );

            return ResponseEntity.ok(guardada);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: verifique FK (id_raza, id_refugio) y enums (sexo/estado)");
        }
    }

    // --- 8. MÉTODO 'delete' ACTUALIZADO ---
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) { // <-- AÑADIDO
        Mascota m = service.get(id);
        if (m == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la mascota con id " + id);
        }
        try {
            service.delete(id);
            // --- 9. LLAMADA AL HELPER ACTUALIZADA ---
            registrarAuditoria(
                    authHeader, // <-- AÑADIDO
                    "Mascota",
                    id.toString(),
                    Auditoria.Accion.DELETE,
                    "se elimino la mascota '" + m.getNombre() + "' (id=" + id + ")"
            );
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: tiene historial o adopcion asociada");
        }
    }

    @GetMapping("/by-zona")
    public ResponseEntity<?> getMascotasPorZona(
            @RequestParam String zona,
            @RequestHeader("Authorization") String authHeader) {

        // (Validación de Token simple)
        Usuario actor = getActorFromToken(authHeader); // Re-usa tu helper de auditoría
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
        }
        // (Validación de Rol)
        if (actor.getRol() == Usuario.Rol.AP) { // Un adoptante no puede usar esto
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }

        List<Mascota> mascotas = service.findByZonaAsignada(zona);
        return ResponseEntity.ok(mascotas);
    }
    private Usuario getActorFromToken(String authHeader) {
        try {
            String token = authHeader.substring(7); // Quita "Bearer "
            String login = jwtUtil.getLoginFromToken(token);
            Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
            return usuarioOpt.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional(readOnly = true) // Es solo lectura
    @GetMapping("/secure-search/{idMascota}")
    public ResponseEntity<?> secureSearchMascota(
            @PathVariable Integer idMascota,
            @RequestHeader("Authorization") String authHeader) {

        Usuario actor = getActorFromToken(authHeader);
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }
        if (actor.getRol() != Usuario.Rol.V) { // Solo para Vets
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado."));
        }

        // 1. Buscar la mascota
        Mascota mascota = service.get(idMascota);
        if (mascota == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Mascota no encontrada con ID: " + idMascota));
        }

        // 2. Buscar el perfil del veterinario
        Optional<Veterinario> vetOpt = veterinarioService.findByUsuario(actor);
        if (vetOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "No se encontró tu perfil de veterinario."));
        }

        // 3. Aplicar regla de especialidad
        String especialidad = vetOpt.get().getEspecialidad().toLowerCase();
        String especieMascota = mascota.getRaza().getEspecie().getNombre().toLowerCase();

        if (especialidad.contains("general")) {
            // "Cirugía General" puede ver todo
            return ResponseEntity.ok(mascota);
        }

        if (especialidad.contains("felina") && !especieMascota.equals("gato")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Tu especialidad es '" + vetOpt.get().getEspecialidad() + "'. Solo puedes ver Gatos."));
        }

        if (especialidad.contains("canina") && !especieMascota.equals("perro")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Tu especialidad es '" + vetOpt.get().getEspecialidad() + "'. Solo puedes ver Perros."));
        }

        // Si pasó los filtros, devuelve la mascota
        return ResponseEntity.ok(mascota);
    }

    // --- 10. HELPER DE AUDITORÍA ACTUALIZADO ---
    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Auditoria.Accion accion, String comentario) {
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
