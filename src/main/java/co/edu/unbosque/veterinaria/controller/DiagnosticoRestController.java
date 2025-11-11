package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Diagnostico;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.DiagnosticoServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/diagnosticos")
public class DiagnosticoRestController {

    @Autowired private DiagnosticoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar todos
    @GetMapping("/getAll")
    public List<Diagnostico> getAll() {
        return service.getAll();
    }

    // obtener por id (Integer)
    @GetMapping("/{id}")
    public ResponseEntity<Diagnostico> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Diagnostico d = service.get(id);
        if (d == null) throw new ResourceNotFoundException("diagnostico no encontrado: " + id);
        return ResponseEntity.ok(d);
    }

    // crear o actualizar diagnostico (permite update)
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Diagnostico d) {
        boolean esNuevo = (d.getIdDiagnostico() == null);

        // fecha por defecto si no llega
        if (d.getFecha() == null) d.setFecha(LocalDateTime.now());

        // si es update, validar existencia
        if (!esNuevo) {
            Diagnostico existente = service.get(d.getIdDiagnostico());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el diagnostico con id " + d.getIdDiagnostico());
            }
        }

        // validaciones minimas
        if (d.getHistorial() == null || d.getHistorial().getIdHistorial() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el historial medico (idHistorial)");
        }
        if (d.getEmpleado() == null || d.getEmpleado().getIdEmpleado() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el empleado que registra el diagnostico (idEmpleado)");
        }
        if (d.getDiagnostico() == null || d.getDiagnostico().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("el campo 'diagnostico' es obligatorio");
        }

        Diagnostico guardado = service.save(d);

        // auditoria
        String comentario = esNuevo
                ? "se creo diagnostico " + guardado.getIdDiagnostico() + " en historial " + guardado.getHistorial().getIdHistorial()
                : "se actualizo diagnostico " + guardado.getIdDiagnostico() + " en historial " + guardado.getHistorial().getIdHistorial();

        registrarAuditoria(
                "Diagnostico",
                guardado.getIdDiagnostico() != null ? guardado.getIdDiagnostico().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    // ===== helpers =====

    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // cuando tengas autenticacion, setea el usuario actor
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
