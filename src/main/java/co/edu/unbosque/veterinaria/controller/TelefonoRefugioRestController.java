package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugio;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugioId;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.TelefonoRefugioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/telefonos-refugio")
public class TelefonoRefugioRestController {

    @Autowired private AuditoriaRegistrador auditoria;

    @Autowired private TelefonoRefugioServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // --- INYECCIONES AÑADIDAS PARA AUDITORÍA ---
    @Autowired private UsuarioServiceAPI usuarioService;
    @Autowired private JwtUtil jwtUtil;

    @GetMapping("/getAll")
    public List<TelefonoRefugio> getAll() {
        return service.getAll();
    }

    @Transactional
    @PostMapping("/save")
    public ResponseEntity<TelefonoRefugio> save(@RequestBody TelefonoRefugio t,
                                                @RequestHeader("Authorization") String authHeader) {

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
        auditoria.registrar(
                authHeader,
                "Telefono_Refugio",
                guardado.getIdRefugio() + "-" + guardado.getTelefono(),
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                esNuevo
                        ? "Se añadió el teléfono " + guardado.getTelefono()
                        : "Se actualizó el teléfono " + guardado.getTelefono() + " a estado " + guardado.getEstado()
        );

        return ResponseEntity.ok(guardado);
    }
    @Transactional
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Integer idRefugio,
                                    @RequestParam String telefono,
                                    @RequestHeader("Authorization") String authHeader) {

        TelefonoRefugioId id = new TelefonoRefugioId(idRefugio, telefono);
        service.delete(id);

        // Registrar auditoría CON el usuario
        auditoria.registrar(
                authHeader,
                "Telefono_Refugio",
                idRefugio + "-" + telefono,
                Accion.DELETE,
                "Se eliminó el teléfono " + telefono + " del refugio " + idRefugio
        );

        return ResponseEntity.ok("Teléfono eliminado correctamente");
    }

}