package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.HashPass;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.LoginRequest;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired private UsuarioServiceAPI usuarioService;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private HashPass hashPass;
    @Autowired private JwtUtil jwtUtil;

    // ===================== QUERIES =====================

    @GetMapping("/getAll")
    public List<Usuario> getAll() {
        return usuarioService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Usuario u = usuarioService.get(id);
        if (u == null) throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        return ResponseEntity.ok(u);
    }

    // ================== CREATE / UPDATE =================

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Usuario incoming) {

        String loginNorm = normalizarLogin(incoming.getLogin());
        if (loginNorm == null || loginNorm.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El login (email) es obligatorio.");
        }
        incoming.setLogin(loginNorm);

        Usuario existentePorId = (incoming.getIdUsuario() != null) ? usuarioService.get(incoming.getIdUsuario()) : null;
        Optional<Usuario> optPorLogin = usuarioService.findByLogin(loginNorm);

        boolean esNuevo;
        if (existentePorId != null) {
            esNuevo = false;
            if (optPorLogin.isPresent() && !Objects.equals(optPorLogin.get().getIdUsuario(), existentePorId.getIdUsuario())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El login '" + loginNorm + "' ya está registrado.");
            }

            if (incoming.getPasswordHash() == null || incoming.getPasswordHash().isBlank()) {
                incoming.setPasswordHash(existentePorId.getPasswordHash());
            } else {
                incoming.setPasswordHash(hashPass.generarHash(existentePorId, incoming.getPasswordHash()));
            }

            if (incoming.getEstado() == null) incoming.setEstado(existentePorId.getEstado());
            if (incoming.getRol() == null) incoming.setRol(existentePorId.getRol());
            incoming.setIdUsuario(existentePorId.getIdUsuario());

        } else {
            esNuevo = true;
            if (optPorLogin.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El login '" + loginNorm + "' ya está registrado.");
            }

            if (incoming.getPasswordHash() == null || incoming.getPasswordHash().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La clave es obligatoria para crear un usuario.");
            }

            incoming.setPasswordHash(hashPass.generarHash(incoming, incoming.getPasswordHash()));
            if (incoming.getEstado() == null) incoming.setEstado(Usuario.Estado.ACTIVO);
        }

        Usuario guardado = usuarioService.save(incoming);

        String comentario = esNuevo
                ? "Se creó un nuevo usuario con login: " + guardado.getLogin()
                : "Se actualizó el usuario con login: " + guardado.getLogin();

        registrarAuditoria(guardado, "Usuario",
                guardado.getIdUsuario() != null ? guardado.getIdUsuario().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario);

        return ResponseEntity.ok(guardado);
    }

    // ======================= LOGIN =======================

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@Valid @RequestBody LoginRequest loginRequest,
                                          HttpServletRequest request) {
        String loginNorm = normalizarLogin(loginRequest.getLogin());
        if (loginNorm == null || loginNorm.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login inválido.");
        }

        Optional<Usuario> usuarioOpt = usuarioService.findByLogin(loginNorm);
        if (usuarioOpt.isEmpty()) {
            registrarAuditoria(null, "Usuario", null, Accion.UPDATE,
                    "Intento de login fallido (usuario no existe): " + loginNorm);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos.");
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getEstado() == Usuario.Estado.INACTIVO) {
            registrarAuditoria(usuario, "Usuario",
                    String.valueOf(usuario.getIdUsuario()), Accion.UPDATE,
                    "Intento de login con cuenta inactiva: " + usuario.getLogin());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("La cuenta está inactiva.");
        }

        String claveHasheada = hashPass.generarHash(usuario, loginRequest.getClave());
        if (!Objects.equals(claveHasheada, usuario.getPasswordHash())) {
            registrarAuditoria(usuario, "Usuario",
                    String.valueOf(usuario.getIdUsuario()), Accion.UPDATE,
                    "Intento de login fallido (clave incorrecta): " + usuario.getLogin());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos.");
        }

        String token = jwtUtil.generateToken(usuario.getLogin());
        registrarAuditoria(usuario, "Usuario",
                String.valueOf(usuario.getIdUsuario()), Accion.UPDATE,
                "Usuario inició sesión correctamente: " + usuario.getLogin() +
                        " | ip=" + request.getRemoteAddr());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "rol", usuario.getRol() != null ? usuario.getRol().toString() : null
        ));
    }

    // ===================== HELPERS =====================

    private String normalizarLogin(String login) {
        return (login == null) ? null : login.toLowerCase().trim();
    }

    private void registrarAuditoria(Usuario actor, String tabla, String idRegistro,
                                    Accion accion, String comentario) {
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
