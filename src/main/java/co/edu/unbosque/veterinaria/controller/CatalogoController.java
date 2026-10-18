package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Catalogo;
import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.utils.GenericServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * CRUD auditado común a los catálogos (Template Method): listar, consultar,
 * guardar con nombre único y estado ACTIVO por defecto, y eliminar.
 * Cada subclase solo define sus textos y, si las tiene, sus reglas extra.
 */
public abstract class CatalogoController<T extends Catalogo> {

    protected final GenericServiceAPI<T, Integer> service;
    private final AuditoriaRegistrador auditoria;

    protected CatalogoController(GenericServiceAPI<T, Integer> service, AuditoriaRegistrador auditoria) {
        this.service = service;
        this.auditoria = auditoria;
    }

    /** Nombre de la tabla para la auditoría, p. ej. "Raza". */
    protected abstract String tabla();

    /** Sustantivo con artículo para los mensajes, p. ej. "la raza". */
    protected abstract String etiqueta();

    /** Motivo mostrado cuando la base de datos impide borrar, p. ej. "está en uso por mascotas". */
    protected abstract String motivoNoBorrable();

    /** Validaciones propias de la subclase; devuelve el error o null si todo está bien. */
    protected String validar(T entidad) {
        return null;
    }

    /** Dos elementos con el mismo nombre chocan solo si comparten ámbito (p. ej. la misma especie). */
    protected boolean mismoAmbito(T a, T b) {
        return true;
    }

    @GetMapping("/getAll")
    public List<T> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> get(@PathVariable Integer id) throws ResourceNotFoundException {
        T entidad = service.get(id);
        if (entidad == null) throw new ResourceNotFoundException(etiqueta() + " no encontrada: " + id);
        return ResponseEntity.ok(entidad);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody T entidad,
                                  @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (entidad.getNombre() == null || entidad.getNombre().isBlank()) {
            return ResponseEntity.badRequest().body("el nombre es obligatorio");
        }
        entidad.setNombre(entidad.getNombre().trim());
        String error = validar(entidad);
        if (error != null) return ResponseEntity.badRequest().body(error);

        boolean esNuevo = entidad.idCatalogo() == null;
        if (esNuevo) {
            if (entidad.getEstado() == null) entidad.setEstado("ACTIVO");
        } else {
            T existente = service.get(entidad.idCatalogo());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe " + etiqueta() + " con id " + entidad.idCatalogo());
            }
            if (entidad.getEstado() == null) entidad.setEstado(existente.getEstado());
        }

        boolean duplicado = service.getAll().stream().anyMatch(otra ->
                entidad.getNombre().equalsIgnoreCase(otra.getNombre())
                        && mismoAmbito(entidad, otra)
                        && (esNuevo || !Objects.equals(otra.idCatalogo(), entidad.idCatalogo())));
        if (duplicado) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("ya existe " + etiqueta() + " con nombre '" + entidad.getNombre() + "'");
        }

        T guardada;
        try {
            guardada = service.save(entidad);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: nombre duplicado o relación inválida");
        }
        auditoria.registrar(authHeader, tabla(), String.valueOf(guardada.idCatalogo()),
                esNuevo ? Accion.INSERT : Accion.UPDATE,
                (esNuevo ? "se creo " : "se actualizo ") + etiqueta() + " '" + guardada.getNombre()
                        + "' (id=" + guardada.idCatalogo() + ")");
        return ResponseEntity.ok(guardada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        T entidad = service.get(id);
        if (entidad == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no existe " + etiqueta() + " con id " + id);
        }
        try {
            service.delete(id);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("no se puede eliminar: " + motivoNoBorrable());
        }
        auditoria.registrar(authHeader, tabla(), id.toString(), Accion.DELETE,
                "se elimino " + etiqueta() + " '" + entidad.getNombre() + "' (id=" + id + ")");
        return ResponseEntity.ok().build();
    }
}
