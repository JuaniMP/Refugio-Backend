package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugio;
import co.edu.unbosque.veterinaria.entity.TelefonoRefugioId;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.TelefonoRefugioServiceAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/telefonos-refugio")
public class TelefonoRefugioRestController {

    @Autowired private TelefonoRefugioServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;

    @GetMapping("/getAll")
    public List<TelefonoRefugio> getAll() {
        return service.getAll();
    }

    @PostMapping("/save")
    public ResponseEntity<TelefonoRefugio> save(@RequestBody TelefonoRefugio t) {
        boolean esNuevo = (t == null || t.getTelefono() == null);

        TelefonoRefugio guardado = service.save(t);

        registrarAuditoria(
                "Telefono_Refugio",
                guardado.getIdRefugio() + "-" + guardado.getTelefono(),
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                esNuevo
                        ? "Se añadió el teléfono " + guardado.getTelefono() + " al refugio " + guardado.getIdRefugio()
                        : "Se actualizó el teléfono " + guardado.getTelefono() + " del refugio " + guardado.getIdRefugio()
        );

        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Integer idRefugio, @RequestParam String telefono) {
        TelefonoRefugioId id = new TelefonoRefugioId(idRefugio, telefono);
        service.delete(id);

        registrarAuditoria(
                "Telefono_Refugio",
                idRefugio + "-" + telefono,
                Accion.DELETE,
                "Se eliminó el teléfono " + telefono + " del refugio " + idRefugio
        );

        return ResponseEntity.ok("Teléfono eliminado correctamente");
    }

    // Helper interno para auditorías
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
