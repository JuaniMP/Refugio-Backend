package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.HistorialMedico;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.HistorialMedicoServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/historiales")
public class HistorialMedicoRestController {

    @Autowired private HistorialMedicoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar
    @GetMapping("/getAll")
    public List<HistorialMedico> getAll() {
        return service.getAll();
    }

    // obtener por id (integer)
    @GetMapping("/{id}")
    public ResponseEntity<HistorialMedico> get(@PathVariable Integer id) throws ResourceNotFoundException {
        HistorialMedico h = service.get(id);
        if (h == null) throw new ResourceNotFoundException("historial no encontrado: " + id);
        return ResponseEntity.ok(h);
    }

    // crear o actualizar historial
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody HistorialMedico h) {
        boolean esNuevo = esInsert(h);

        // validaciones basicas
        if (h.getMascota() == null || h.getMascota().getIdMascota() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar la mascota (idMascota)");
        }
        // notas puede ser null; si llega, normalizar trim
        if (h.getNotas() != null) h.setNotas(h.getNotas().trim());

        // si es update, validar que exista
        if (!esNuevo) {
            HistorialMedico existente = service.get(h.getIdHistorial());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el historial con id " + h.getIdHistorial());
            }
        }

        HistorialMedico guardado = service.save(h);

        // auditoria
        registrarAuditoria(
                "Historial_Medico",
                guardado.getIdHistorial() != null ? guardado.getIdHistorial().toString() : null,
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                esNuevo
                        ? "se creo historial para la mascota " + h.getMascota().getIdMascota()
                        : "se actualizo historial " + guardado.getIdHistorial() + " de la mascota " + h.getMascota().getIdMascota()
        );

        return ResponseEntity.ok(guardado);
    }

    // ================= helpers internos =================

    // decide si es insert o update
    private boolean esInsert(HistorialMedico h) {
        if (h.getIdHistorial() == null) return true;
        return service.get(h.getIdHistorial()) == null;
    }

    // registra la fila de auditoria
    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // cuando tengas autenticacion, asigna el usuario actor
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
