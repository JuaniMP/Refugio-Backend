package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Adoptante;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AdoptanteServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.HashPass;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/adoptantes")
public class AdoptanteRestController {

    @Autowired private AdoptanteServiceAPI adoptanteService;
    @Autowired private UsuarioServiceAPI usuarioService;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private HashPass hashPass;
    @Autowired private JwtUtil jwtUtil;

    // =========================
    //        QUERIES
    // =========================

    @GetMapping("/getAll")
    public List<Adoptante> getAll() {
        return adoptanteService.getAll();
    }

    @GetMapping("/{id}") // ID es Integer
    public Adoptante get(@PathVariable Integer id) throws ResourceNotFoundException {
        Adoptante a = adoptanteService.get(id);
        if (a == null) throw new ResourceNotFoundException("Adoptante no encontrado: " + id);
        return a;
    }

    // Perfil propio por JWT
    @GetMapping("/me")
    public ResponseEntity<?> getMiPerfil(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token no válido o ausente.");
        }

        String token = authHeader.substring(7);
        String login;
        try {
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
            }
            login = jwtUtil.getLoginFromToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error al procesar token.");
        }

        Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Optional<Adoptante> adoptanteOpt = adoptanteService.findByUsuario(usuarioOpt.get());
        if (adoptanteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Perfil de adoptante no encontrado.");
        }

        return ResponseEntity.ok(adoptanteOpt.get());
    }

    // =========================
    //   CREAR / ACTUALIZAR
    // =========================

    /**
     * Crea o actualiza un Adoptante.
     * - Si viene Usuario nuevo (sin id): se registra (valida login único, hashea clave, rol AP, estado ACTIVO).
     * - Si viene Usuario existente (con id): actualiza login (normalizado).
     * - Valida duplicidad de documento de Adoptante.
     * - Audita INSERT/UPDATE.
     */
    @Transactional
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Adoptante a) {

        // === 0) Normalizar flags y cargar existente si aplica ===
        boolean esNuevoAdoptante = (a.getIdAdoptante() == null);
        Adoptante existente = null;

        if (!esNuevoAdoptante) {
            existente = adoptanteService.get(a.getIdAdoptante());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el adoptante con id " + a.getIdAdoptante());
            }
        }

        // === 1) Validar duplicidad de documento (para nuevos o cuando cambia) ===
        if (a.getDocumento() != null) {
            for (Adoptante otro : adoptanteService.getAll()) {
                if (a.getDocumento().equalsIgnoreCase(otro.getDocumento())) {
                    if (esNuevoAdoptante || !Objects.equals(otro.getIdAdoptante(), a.getIdAdoptante())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Ya existe un adoptante con el documento " + a.getDocumento());
                    }
                }
            }
        }

        // === 2) Manejar Usuario asociado (registro o actualización) ===
        if (a.getUsuario() == null) {
            // Si es registro inicial de adoptante, el usuario es obligatorio (según tu flujo)
            // Si quieres permitir adoptante sin usuario, cambia esta validación.
            return ResponseEntity.badRequest().body("Los datos de usuario son obligatorios.");
        }

        Usuario datosUsuario = a.getUsuario();
        boolean usuarioEsNuevo = (datosUsuario.getIdUsuario() == null);

        if (usuarioEsNuevo) {
            // ----- Registro de usuario -----
            if (datosUsuario.getPasswordHash() == null || datosUsuario.getPasswordHash().isBlank()) {
                return ResponseEntity.badRequest().body("La clave es obligatoria para usuarios nuevos.");
            }

            String loginNormalizado = normalizarLogin(datosUsuario.getLogin());
            if (usuarioService.findByLogin(loginNormalizado).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El correo electrónico '" + loginNormalizado + "' ya está registrado.");
            }

            datosUsuario.setLogin(loginNormalizado);
            // IMPORTANTE: aquí 'passwordHash' trae la clave en texto plano desde el front.
            // Usamos el mismo método que ya tienen para hashearla:
            datosUsuario.setPasswordHash(hashPass.generarHash(datosUsuario, datosUsuario.getPasswordHash()));
            datosUsuario.setRol(Usuario.Rol.AP);
            datosUsuario.setEstado(Usuario.Estado.ACTIVO);

            Usuario userGuardado = usuarioService.save(datosUsuario);
            a.setUsuario(userGuardado);

        } else {
            // ----- Actualización de usuario existente (solo login / email) -----
            Usuario userExistente = usuarioService.get(datosUsuario.getIdUsuario());
            if (userExistente == null) {
                return ResponseEntity.badRequest().body("Usuario a actualizar no encontrado.");
            }

            String loginNormalizado = normalizarLogin(datosUsuario.getLogin());
            // Validar colisión de login con otros usuarios
            Optional<Usuario> colision = usuarioService.findByLogin(loginNormalizado);
            if (colision.isPresent() && !Objects.equals(colision.get().getIdUsuario(), userExistente.getIdUsuario())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El correo electrónico '" + loginNormalizado + "' ya está registrado.");
            }

            userExistente.setLogin(loginNormalizado);
            Usuario userGuardado = usuarioService.save(userExistente);
            a.setUsuario(userGuardado);

            // (Si algún día permites cambiar contraseña aquí, validar y hashear como arriba)
        }

        // === 3) Guardar Adoptante ===
        Adoptante guardado = adoptanteService.save(a);

        // === 4) Auditoría ===
        registrarAuditoria(
                "Adoptante",
                guardado.getIdAdoptante() != null ? guardado.getIdAdoptante().toString() : null,
                esNuevoAdoptante ? Auditoria.Accion.INSERT : Auditoria.Accion.UPDATE,
                esNuevoAdoptante
                        ? "Se registró un nuevo adoptante: " + guardado.getNombre()
                        : "Se actualizó la información del adoptante: " + guardado.getNombre()
        );

        return ResponseEntity.ok(guardado);
    }

    // =========================
    //         DELETE
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            Adoptante a = adoptanteService.get(id);
            if (a == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No existe el adoptante con id " + id);
            }

            adoptanteService.delete(id);

            registrarAuditoria(
                    "Adoptante",
                    id.toString(),
                    Auditoria.Accion.DELETE,
                    "Se eliminó el adoptante con id " + id + " (" + a.getNombre() + ")"
            );

            return ResponseEntity.ok().build();

        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: tiene solicitudes/adopciones.");
        }
    }

    // =========================
    //       HELPERS
    // =========================

    private void registrarAuditoria(String tabla, String idRegistro, Auditoria.Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // TODO: si luego amarras al usuario autenticado, ponlo acá
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }

    private String normalizarLogin(String login) {
        return login == null ? null : login.toLowerCase().trim();
    }
}
