package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.security.SesionActual;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.EmailService;
import co.edu.unbosque.veterinaria.utils.HashPass;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.LoginRequest;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import java.util.regex.Pattern;

import jakarta.validation.Valid;
// import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired private AuditoriaRegistrador auditoria;
    @Autowired private SesionActual sesionActual;

    // --- CORREGIDO: Se cambió Field Injection por Constructor Injection ---
    private final UsuarioServiceAPI usuarioService;
    private final AuditoriaServiceAPI auditoriaService;
    private final HashPass hashPass;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Autowired
    public UsuarioRestController(UsuarioServiceAPI usuarioService,
                                 AuditoriaServiceAPI auditoriaService,
                                 HashPass hashPass,
                                 JwtUtil jwtUtil,
                                 EmailService emailService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
        this.hashPass = hashPass;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El login (email) es obligatorio."));
        }
        incoming.setLogin(loginNorm);

        Usuario existentePorId = (incoming.getIdUsuario() != null) ? usuarioService.get(incoming.getIdUsuario()) : null;
        Optional<Usuario> optPorLogin = usuarioService.findByLogin(loginNorm);

        boolean esNuevo;
        if (existentePorId != null) {
            esNuevo = false;
            if (optPorLogin.isPresent() && !Objects.equals(optPorLogin.get().getIdUsuario(), existentePorId.getIdUsuario())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "El login '" + loginNorm + "' ya está registrado."));
            }

            if (incoming.getPasswordHash() == null || incoming.getPasswordHash().isBlank()) {
                incoming.setPasswordHash(existentePorId.getPasswordHash());
            } else {
                incoming.setPasswordHash(hashPass.hashear(incoming.getPasswordHash()));
            }

            if (incoming.getEstado() == null) incoming.setEstado(existentePorId.getEstado());
            if (incoming.getRol() == null) incoming.setRol(existentePorId.getRol());
            incoming.setIdUsuario(existentePorId.getIdUsuario());

        } else {
            esNuevo = true;
            if (optPorLogin.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "El login '" + loginNorm + "' ya está registrado."));
            }

            if (incoming.getPasswordHash() == null || incoming.getPasswordHash().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "La clave es obligatoria para crear un usuario."));
            }

            incoming.setPasswordHash(hashPass.hashear(incoming.getPasswordHash()));
            if (incoming.getEstado() == null) incoming.setEstado(Usuario.Estado.ACTIVO);
        }

        Usuario guardado = usuarioService.save(incoming);

        String comentario = esNuevo
                ? "Se creó un nuevo usuario con login: " + guardado.getLogin()
                : "Se actualizó el usuario con login: " + guardado.getLogin();

        auditoria.registrar(guardado, "Usuario",
                guardado.getIdUsuario() != null ? guardado.getIdUsuario().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario);

        return ResponseEntity.ok(guardado);
    }

    // ======================= LOGIN =======================

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@Valid @RequestBody LoginRequest loginRequest) {
        String loginNorm = normalizarLogin(loginRequest.getLogin());
        if (loginNorm == null || loginNorm.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Login inválido."));
        }

        Optional<Usuario> usuarioOpt = usuarioService.findByLogin(loginNorm);
        if (usuarioOpt.isEmpty()) {
            auditoria.registrar((Usuario) null, "Usuario", null, Accion.UPDATE, "Intento de login fallido (usuario no existe): " + loginNorm);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Usuario o contraseña incorrectos."));
        }

        Usuario usuario = usuarioOpt.get();

        // Lógica de Adoptante INACTIVO (la dejamos como estaba)
        if (usuario.getEstado() == Usuario.Estado.INACTIVO) {
            if (usuario.getVerificationCode() != null && usuario.getVerificationCodeExpires() != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "ACCOUNT_INACTIVE", "message", "La cuenta está inactiva. Por favor, revisa tu email y verifica tu cuenta."));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "ACCOUNT_DISABLED", "message", "La cuenta está inactiva."));
        }

        // Validación de hash de contraseña
        if (!hashPass.coincide(usuario, loginRequest.getClave())) {

            auditoria.registrar(usuario, "Usuario", usuario.getIdUsuario().toString(),
                    Accion.UPDATE, "Intento de login fallido (clave incorrecta) para: " + usuario.getLogin());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Usuario o contraseña incorrectos."));
        }

        // Cuentas antiguas (SHA-256): se re-guardan con BCrypt al iniciar sesión
        if (hashPass.esLegado(usuario)) {
            usuario.setPasswordHash(hashPass.hashear(loginRequest.getClave()));
            usuarioService.save(usuario);
        }

        String token = jwtUtil.generateToken(usuario.getLogin());

        auditoria.registrar(usuario, "Usuario", usuario.getIdUsuario().toString(),
                Accion.LOGIN, "Login exitoso para: " + usuario.getLogin());

        if (usuario.isRequiresPasswordChange()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "FORCE_RESET", // Error especial
                            "message", "Debes cambiar tu contraseña temporal.",
                            "token", token, // Le damos el token para que pueda hacer el cambio
                            "rol", usuario.getRol() != null ? usuario.getRol().toString() : null
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "token", token,
                "rol", usuario.getRol() != null ? usuario.getRol().toString() : null
        ));
    }

    // ===============================================
    // ===============================================

    /**
     * Activa una cuenta de usuario usando el código de verificación.
     * Recibe: { "login": "correo@...", "code": "123456" }
     */
    @PostMapping("/verify-account")
    public ResponseEntity<?> verifyAccount(@RequestBody Map<String, String> body) {
        String login = normalizarLogin(body.get("login"));
        String code = body.get("code");

        if (login == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Login y código son requeridos."));
        }

        Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado."));
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getEstado() == Usuario.Estado.ACTIVO) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "La cuenta ya está activa."));
        }

        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(code)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Código incorrecto."));
        }

        if (usuario.getVerificationCodeExpires() != null && Instant.now().isAfter(usuario.getVerificationCodeExpires())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El código ha expirado."));
        }

        // ¡Éxito! Activamos la cuenta
        usuario.setEstado(Usuario.Estado.ACTIVO);
        usuario.setVerificationCode(null);
        usuario.setVerificationCodeExpires(null);
        usuarioService.save(usuario);

        auditoria.registrar(usuario, "Usuario", usuario.getIdUsuario().toString(),
                Accion.UPDATE, "Cuenta activada exitosamente por código.");

        return ResponseEntity.ok(Map.of("message", "Cuenta activada exitosamente."));
    }

    /**
     * Inicia el proceso de olvido de contraseña. Envía un código al email.
     * Recibe: { "login": "correo@..." }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String login = normalizarLogin(body.get("login"));
        if (login == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El login (email) es requerido."));
        }

        Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            // No revelamos si el usuario existe o no por seguridad
            return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás un código."));
        }

        Usuario usuario = usuarioOpt.get();

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        // Guardar código y expiración (15 minutos)
        usuario.setVerificationCode(codigo);
        usuario.setVerificationCodeExpires(Instant.now().plus(15, ChronoUnit.MINUTES));
        usuarioService.save(usuario);

        // Enviar el correo
        try {
            emailService.sendPasswordResetEmail(usuario.getLogin(), codigo);
        } catch (Exception e) {
            System.err.println("Error al enviar email de reseteo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error al enviar el correo."));
        }

        auditoria.registrar(usuario, "Usuario", usuario.getIdUsuario().toString(),
                Accion.UPDATE, "Solicitó reseteo de contraseña.");

        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás un código."));
    }

    /**
     * Completa el reseteo de contraseña usando el código.
     * Recibe: { "login": "correo@...", "code": "123456", "nuevaClave": "..." }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String login = normalizarLogin(body.get("login"));
        String code = body.get("code");
        String nuevaClave = body.get("nuevaClave");

        if (login == null || code == null || nuevaClave == null || nuevaClave.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Login, código y nueva clave son requeridos."));
        }

        Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Código o usuario inválido."));
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(code)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Código incorrecto."));
        }

        if (usuario.getVerificationCodeExpires() != null && Instant.now().isAfter(usuario.getVerificationCodeExpires())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El código ha expirado."));
        }

        // --- VALIDACIONES DE SEGURIDAD ---

        // 1. Verificar si es la misma que la actual
        if (hashPass.coincide(usuario, nuevaClave)) {
            return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict
                    .body(Map.of("message", "La nueva contraseña no puede ser igual a la contraseña actual.")); // (Esto ya lo tenías)
        }

        // 2. Verificar si es fuerte
        if (!isPasswordStrong(nuevaClave)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "La contraseña no es segura. Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial.")); // (Esto ya lo tenías)
        }

        // --- ÉXITO ---
        usuario.setPasswordHash(hashPass.hashear(nuevaClave));
        usuario.setVerificationCode(null);
        usuario.setVerificationCodeExpires(null);
        usuario.setEstado(Usuario.Estado.ACTIVO);
        usuario.setRequiresPasswordChange(false);

        usuarioService.save(usuario);

        auditoria.registrar(usuario, "Usuario", usuario.getIdUsuario().toString(),
                Accion.UPDATE, "Contraseña restablecida exitosamente.");

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente."));
    }
    @PostMapping("/force-reset-password")
    public ResponseEntity<?> forceResetPassword(@RequestBody Map<String, String> body,
                                                @RequestHeader("Authorization") String authHeader) {

        String nuevaClave = body.get("nuevaClave");
        if (nuevaClave == null || nuevaClave.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "La nueva clave es obligatoria."));
        }

        // 1. Obtener el usuario DESDE EL TOKEN (más seguro)
        Usuario actor = sesionActual.usuario(authHeader);
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        // 2. Validar fortaleza
        if (!isPasswordStrong(nuevaClave)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "La contraseña no es segura. Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial."));
        }

        // 3. Validar que no sea la misma clave
        if (hashPass.coincide(actor, nuevaClave)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "La nueva contraseña no puede ser igual a la contraseña temporal."));
        }

        // 4. Actualizar usuario
        actor.setPasswordHash(hashPass.hashear(nuevaClave));
        actor.setRequiresPasswordChange(false); // ¡IMPORTANTE!
        usuarioService.save(actor);

        auditoria.registrar(actor, "Usuario", actor.getIdUsuario().toString(),
                Accion.UPDATE, "Empleado cambió su contraseña temporal exitosamente.");

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada."));
    }
    // ===================== HELPERS =====================
    private String normalizarLogin(String login) {
        return (login == null) ? null : login.toLowerCase().trim();
    }

    // Helper para fortaleza de contraseña
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        String strongPasswordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

        return Pattern.compile(strongPasswordPattern)
                .matcher(password)
                .matches();
    }
}