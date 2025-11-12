package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugio;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugioId;
import co.edu.unbosque.veterinaria.entity.Usuario; // <-- IMPORTADO
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.TelefonoRefugioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI; // <-- IMPORTADO
import co.edu.unbosque.veterinaria.utils.JwtUtil; // <-- IMPORTADO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // <-- IMPORTADO

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/telefonos-refugio")
public class TelefonoRefugioRestController {

    @Autowired private TelefonoRefugioServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- INYECCIONES AÑADIDAS PARA AUDITORÍA ---
    @Autowired private UsuarioServiceAPI usuarioService;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/getAll")
    public List<TelefonoRefugio> getAll() {
        return service.getAll();
    }

    @PostMapping("/save")
    public ResponseEntity<TelefonoRefugio> save(@RequestBody TelefonoRefugio t,
                                                @RequestHeader("Authorization") String authHeader) { // <-- Se pide el Token

        // Lógica mejorada para saber si es nuevo o actualización
        Optional<TelefonoRefugio> existente = Optional.ofNullable(service.get(new TelefonoRefugioId(t.getIdRefugio(), t.getTelefono())));
        boolean esNuevo = existente.isEmpty();

        // Si es nuevo, aseguramos que esté ACTIVO (gracias al @Builder.Default)
        // Si es una actualización (ej. cambiar estado), el 'estado' vendrá en el body 't'
        if (esNuevo) {
            t.setEstado("ACTIVO");
        }

        TelefonoRefugio guardado = service.save(t);

        // Registrar auditoría CON el usuario
        registrarAuditoria(
                authHeader, // <-- Se pasa el Token
                "Telefono_Refugio",
                guardado.getIdRefugio() + "-" + guardado.getTelefono(),
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                esNuevo
                        ? "Se añadió el teléfono " + guardado.getTelefono()
                        : "Se actualizó el teléfono " + guardado.getTelefono() + " a estado " + guardado.getEstado()
        );

        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Integer idRefugio,
                                    @RequestParam String telefono,
                                    @RequestHeader("Authorization") String authHeader) { // <-- Se pide el Token

        TelefonoRefugioId id = new TelefonoRefugioId(idRefugio, telefono);
        service.delete(id);

        // Registrar auditoría CON el usuario
        registrarAuditoria(
                authHeader, // <-- Se pasa el Token
                "Telefono_Refugio",
                idRefugio + "-" + telefono,
                Accion.DELETE,
                "Se eliminó el teléfono " + telefono + " del refugio " + idRefugio
        );

        return ResponseEntity.ok("Teléfono eliminado correctamente");
    }

    // --- Helper interno para auditorías (AHORA RECIBE EL TOKEN) ---
    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Auditoria.Accion accion, String comentario) {
        try {
            // Extraer el usuario del token
            String token = authHeader.substring(7);
            String login = jwtUtil.getLoginFromToken(token);
            Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);

            // Solo registrar si el usuario es válido
            if (usuarioOpt.isPresent()) {
                Auditoria aud = Auditoria.builder()
                        .usuario(usuarioOpt.get()) // <-- USUARIO REAL AÑADIDO
                        .tablaAfectada(tabla)
                        .idRegistro(idRegistro)
                        .accion(accion)
                        .comentarioAuditoria(comentario) // Asumiendo que el campo es 'detalleJson'
                        .build();
                auditoriaService.save(aud);
            }
        } catch (Exception e) {
            // Si la auditoría falla, al menos lo imprimimos en la consola del backend
            System.err.println("Error al guardar auditoría: " + e.getMessage());
        }
    }
}