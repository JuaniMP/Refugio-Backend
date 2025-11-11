package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.HashPass;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired private UsuarioServiceAPI usuarioService;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private HashPass hashPass;

    // listar todos
    @GetMapping("/getAll")
    public List<Usuario> getAll() {
        return usuarioService.getAll();
    }

    // obtener por id (Integer)
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Usuario u = usuarioService.get(id);
        if (u == null) throw new ResourceNotFoundException("usuario no encontrado: " + id);
        return ResponseEntity.ok(u);
    }

    // crear o actualizar con reglas: upsert por id o por login
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Usuario usuario) {
        // 1) normalizar login
        normalizarLogin(usuario);
        if (usuario.getLogin() == null || usuario.getLogin().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("login es obligatorio");
        }

        // 2) cargar existentes por id y por login
        Usuario existentePorId = (usuario.getIdUsuario() != null) ? usuarioService.get(usuario.getIdUsuario()) : null;
        Usuario existentePorLogin = findByLogin(usuario.getLogin());

        // 3) decidir si es insert o update
        boolean esNuevo;
        if (existentePorId != null) {
            // viene con id valido -> update por id
            esNuevo = false;

            // si cambia login a uno ya tomado por otro usuario -> 409
            if (existentePorLogin != null && !Objects.equals(existentePorLogin.getIdUsuario(), existentePorId.getIdUsuario())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("el login " + usuario.getLogin() + " ya esta registrado");
            }
        } else {
            if (usuario.getIdUsuario() == null && existentePorLogin != null) {
                // no vino id, pero el login existe -> update por login
                usuario.setIdUsuario(existentePorLogin.getIdUsuario());
                esNuevo = false;
            } else {
                // no existe ni por id ni por login -> insert
                esNuevo = true;

                // por seguridad, si alguien manda insert con login ya tomado (caso borde)
                if (existentePorLogin != null) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("el login " + usuario.getLogin() + " ya esta registrado");
                }
            }
        }

        // 4) hashear la clave (se asume que viene en texto plano en passwordHash)
        usuario.setPasswordHash(hashPass.generarHash(usuario, usuario.getPasswordHash()));

        // 5) estado por defecto si es nuevo
        if (esNuevo && usuario.getEstado() == null) {
            usuario.setEstado(Usuario.Estado.ACTIVO);
        }

        // 6) guardar
        Usuario guardado = usuarioService.save(usuario);

        // 7) auditoria
        String comentario = esNuevo
                ? "se creo un nuevo usuario con login: " + guardado.getLogin()
                : "se actualizo el usuario con login: " + guardado.getLogin();

        registrarAuditoria(
                guardado,
                "Usuario",
                guardado.getIdUsuario() != null ? guardado.getIdUsuario().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // ===================== helpers =====================

    private void normalizarLogin(Usuario u) {
        if (u.getLogin() != null) {
            u.setLogin(u.getLogin().toLowerCase().trim());
        }
    }

    // busca por login ignorando mayusculas/minusculas usando el service generico
    // nota: como no queremos crear repo/metodo nuevo, hacemos un filtro en memoria
    private Usuario findByLogin(String loginNormalizado) {
        if (loginNormalizado == null) return null;
        return usuarioService.getAll()
                .stream()
                .filter(x -> x.getLogin() != null && x.getLogin().equalsIgnoreCase(loginNormalizado))
                .findFirst()
                .orElse(null);
    }

    private void registrarAuditoria(Usuario actor, String tabla, String idRegistro, Accion accion, String comentario) {
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
