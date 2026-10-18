package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.security.SesionActual;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.HistorialMedicoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/historiales")
public class HistorialMedicoRestController {

    @Autowired private AuditoriaRegistrador auditoria;
    @Autowired private SesionActual sesionActual;

    @Autowired private HistorialMedicoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    @GetMapping("/getAll")
    public List<HistorialMedico> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<HistorialMedico> get(@PathVariable Integer id) throws ResourceNotFoundException {
        HistorialMedico h = service.get(id);
        if (h == null) throw new ResourceNotFoundException("historial no encontrado: " + id);
        return ResponseEntity.ok(h);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody HistorialMedico h,
                                  @RequestHeader("Authorization") String authHeader) {
        boolean esNuevo = esInsert(h);

        if (h.getMascota() == null || h.getMascota().getIdMascota() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar la mascota (idMascota)");
        }
        if (h.getNotas() != null) h.setNotas(h.getNotas().trim());
        if (!esNuevo) {
            HistorialMedico existente = service.get(h.getIdHistorial());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el historial con id " + h.getIdHistorial());
            }
        }

        HistorialMedico guardado = service.save(h);

        String comentario = esNuevo
                ? "se creo historial para la mascota " + h.getMascota().getIdMascota()
                : "se actualizo historial " + guardado.getIdHistorial() + " de la mascota " + h.getMascota().getIdMascota();

        auditoria.registrar(
                authHeader,
                "Historial_Medico",
                guardado.getIdHistorial() != null ? guardado.getIdHistorial().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }
    // --- ⬇️ ENDPOINT NUEVO A AÑADIR ⬇️ ---
    @GetMapping("/by-mascota/{idMascota}")
    public ResponseEntity<?> getHistorialPorMascota(
            @PathVariable Integer idMascota,
            @RequestHeader("Authorization") String authHeader) {

        // (Validación simple de token)
        if (sesionActual.usuario(authHeader) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        Optional<HistorialMedico> historialOpt = service.findByMascotaId(idMascota);
        if (historialOpt.isEmpty()) {
            // No es un error, creamos uno nuevo para esta mascota
            HistorialMedico nuevoHistorial = new HistorialMedico();
            nuevoHistorial.setMascota(new co.edu.unbosque.veterinaria.entity.Mascota());
            nuevoHistorial.getMascota().setIdMascota(idMascota);
            // Guardamos un historial vacío
            HistorialMedico historialGuardado = service.save(nuevoHistorial);
            return ResponseEntity.ok(historialGuardado);
        }

        return ResponseEntity.ok(historialOpt.get());
    }

    // ================= helpers internos =================

    private boolean esInsert(HistorialMedico h) {
        if (h.getIdHistorial() == null) return true;
        return service.get(h.getIdHistorial()) == null;
    }

}