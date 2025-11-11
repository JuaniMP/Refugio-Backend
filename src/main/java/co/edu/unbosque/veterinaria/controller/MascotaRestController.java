package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.MascotaServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/mascotas")
public class MascotaRestController {

    @Autowired private MascotaServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    @GetMapping("/getAll")
    public List<Mascota> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Mascota m = service.get(id);
        if (m == null) throw new ResourceNotFoundException("mascota no encontrada: " + id);
        return ResponseEntity.ok(m);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Mascota m) {
        boolean esNuevo = (m.getIdMascota() == null);

        // Validaciones que sí existen en la tabla
        if (m.getNombre() == null || m.getNombre().isBlank())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("el nombre es obligatorio");

        if (m.getRaza() == null || m.getRaza().getIdRaza() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar la raza (idRaza)");

        if (m.getRefugio() == null || m.getRefugio().getIdRefugio() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("debes enviar el refugio (idRefugio)");

        try {
            Mascota guardada = service.save(m);

            registrarAuditoria(
                    "Mascota",
                    guardada.getIdMascota() != null ? guardada.getIdMascota().toString() : null,
                    esNuevo ? Auditoria.Accion.INSERT : Auditoria.Accion.UPDATE,
                    (esNuevo ? "se registro" : "se actualizo") + " la mascota '" + guardada.getNombre() + "'"
            );

            return ResponseEntity.ok(guardada);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: verifique FK (id_raza, id_refugio) y enums (sexo/estado)");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        Mascota m = service.get(id);
        if (m == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe la mascota con id " + id);
        }
        try {
            service.delete(id);
            registrarAuditoria(
                    "Mascota",
                    id.toString(),
                    Auditoria.Accion.DELETE,
                    "se elimino la mascota '" + m.getNombre() + "' (id=" + id + ")"
            );
            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: tiene historial o adopcion asociada");
        }
    }

    private void registrarAuditoria(String tabla, String idRegistro, Auditoria.Accion accion, String comentario) {
        Auditoria aud = Auditoria.builder()
                .usuario(null)
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }
}
