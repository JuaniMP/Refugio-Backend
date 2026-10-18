package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.security.SesionActual;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.MascotaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import org.springframework.transaction.annotation.Transactional;import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaRestController {

    @Autowired private AuditoriaRegistrador auditoria;
    @Autowired private SesionActual sesionActual;

    @Autowired private MascotaServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

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

    @Transactional
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Mascota m,
                                  @RequestHeader("Authorization") String authHeader) {
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

            auditoria.registrar(
                    authHeader,
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

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) {
        Mascota m = service.get(id);
        if (m == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la mascota con id " + id);
        }
        try {
            service.delete(id);
            auditoria.registrar(
                    authHeader,
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
        Usuario actor = sesionActual.usuario(authHeader); // Re-usa tu helper de auditoría
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

    @Transactional(readOnly = true) // Es solo lectura
    @GetMapping("/secure-search/{idMascota}")
    public ResponseEntity<?> secureSearchMascota(
            @PathVariable Integer idMascota,
            @RequestHeader("Authorization") String authHeader) {

        Usuario actor = sesionActual.usuario(authHeader);
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

}
