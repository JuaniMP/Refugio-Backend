package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.AplicacionVacuna;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.service.api.AplicacionVacunaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/aplicaciones-vacuna")
public class AplicacionVacunaRestController {

    @Autowired private AplicacionVacunaServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    // listar todas las aplicaciones de vacuna
    @GetMapping("/getAll")
    public List<AplicacionVacuna> getAll() {
        return service.getAll();
    }

    // obtener una por id
    @GetMapping("/{id}")
    public AplicacionVacuna get(@PathVariable Integer id) throws ResourceNotFoundException {
        AplicacionVacuna a = service.get(id);
        if (a == null) throw new ResourceNotFoundException("aplicacion de vacuna no encontrada: " + id);
        return a;
    }

    // crear nueva aplicación (no se permite update)
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody AplicacionVacuna a) {
        if (a.getIdAplicacion() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("no se permite actualizar una aplicacion de vacuna existente; no debes enviar idAplicacion");
        }

        // validar datos minimos
        if (a.getHistorial() == null || a.getHistorial().getIdHistorial() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el historial medico (idHistorial)");
        }
        if (a.getVacuna() == null || a.getVacuna().getIdVacuna() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar la vacuna aplicada (idVacuna)");
        }
        if (a.getFecha() == null) {
            a.setFecha(LocalDate.now());
        }

        AplicacionVacuna guardada = service.save(a);

        // registrar auditoría
        registrarAuditoria(
                "Aplicacion_Vacuna",
                guardada.getIdAplicacion().toString(),
                Accion.INSERT,
                "se registro una nueva aplicacion de vacuna " +
                        guardada.getVacuna().getIdVacuna() +
                        " en historial " + guardada.getHistorial().getIdHistorial()
        );

        return ResponseEntity.ok(guardada);
    }

    // eliminar aplicación (por error o revisión)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        AplicacionVacuna a = service.get(id);
        if (a == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la aplicacion de vacuna con id " + id);
        }

        try {
            service.delete(id);
            registrarAuditoria(
                    "Aplicacion_Vacuna",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino la aplicacion de vacuna con id " + id
            );
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: esta vinculada a otros registros");
        }
    }

    // helper para crear fila de auditoria
    private void registrarAuditoria(String tabla, String idRegistro, Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null) // se puede llenar luego con el usuario logueado
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
