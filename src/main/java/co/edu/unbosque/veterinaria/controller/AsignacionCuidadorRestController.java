package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.AsignacionCuidador;
import co.edu.unbosque.veterinaria.entity.AsignacionCuidadorId;
import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.service.api.AsignacionCuidadorServiceAPI;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.CuidadorServiceAPI;
import co.edu.unbosque.veterinaria.service.api.MascotaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors; // <-- AÑADIR IMPORT

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionCuidadorRestController {

    @Autowired private AsignacionCuidadorServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    @Autowired private CuidadorServiceAPI cuidadorService;
    @Autowired private MascotaServiceAPI mascotaService;


    @GetMapping("/getAll")
    public List<AsignacionCuidador> getAll() {
        return service.getAll();
    }

    // ===============================================
    // --- ENDPOINTS DE TURNO CORREGIDOS ---
    // ===============================================

    /**
     * Devuelve las asignaciones activas (turno sin cerrar) para el cuidador logueado.
     */
    @GetMapping("/mi-turno-activo")
    public ResponseEntity<?> getTurnoActivo(@RequestHeader("Authorization") String authHeader) {
        // 1. Obtener usuario (del token)
        Usuario actor = getActorFromToken(authHeader);
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        // 2. Obtener cuidador (del usuario)
        Optional<Cuidador> cuidadorOpt = cuidadorService.findByUsuario(actor);
        if (cuidadorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Perfil de cuidador no encontrado."));
        }

        // 3. ¡LA CORRECCIÓN! Usar el ID de Empleado del cuidador
        Integer idEmpleado = cuidadorOpt.get().getIdEmpleado();
        List<AsignacionCuidador> turnoActivo = service.findActivasByIdEmpleado(idEmpleado);

        return ResponseEntity.ok(turnoActivo);
    }

    /**
     * Inicia un nuevo turno para el cuidador logueado.
     * Asigna todas las mascotas de su zona.
     */
    @Transactional
    @PostMapping("/comenzar-turno")
    public ResponseEntity<?> comenzarTurno(@RequestHeader("Authorization") String authHeader) {
        Usuario actor = getActorFromToken(authHeader);
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        // 1. Buscar al cuidador y su zona
        Optional<Cuidador> cuidadorOpt = cuidadorService.findByUsuario(actor);
        if (cuidadorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No se encontró tu perfil de cuidador."));
        }
        Cuidador cuidador = cuidadorOpt.get();
        Integer idEmpleado = cuidador.getIdEmpleado();
        String zona = cuidador.getZonaAsignada();

        // 2. Verificar que no tenga un turno activo (¡CORREGIDO!)
        if (!service.findActivasByIdEmpleado(idEmpleado).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Ya tienes un turno activo. Termínalo antes de iniciar uno nuevo."));
        }

        if (zona == null || zona.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "No tienes una zona asignada. Contacta al administrador."));
        }

        // 3. Buscar mascotas en esa zona (que estén EN_REFUGIO)
        List<Mascota> mascotasEnZona = mascotaService.findByZonaAsignada(zona).stream()
                .filter(m -> m.getEstado() == Mascota.Estado.EN_REFUGIO)
                .collect(Collectors.toList());

        if (mascotasEnZona.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No hay mascotas EN REFUGIO en tu zona asignada (" + zona + ") en este momento."));
        }

        // 4. Crear las nuevas asignaciones
        LocalDate fechaInicio = LocalDate.now();
        List<AsignacionCuidador> nuevasAsignaciones = new ArrayList<>();

        for (Mascota mascota : mascotasEnZona) {
            AsignacionCuidador nueva = AsignacionCuidador.builder()
                    .idMascota(mascota.getIdMascota())
                    .idEmpleado(idEmpleado)
                    .fechaInicio(fechaInicio)
                    .fechaFin(null)
                    .comentarios(null)
                    .build();
            nuevasAsignaciones.add(service.save(nueva));
        }

        registrarAuditoria(authHeader, "Asignacion_Cuidador", idEmpleado.toString(), Accion.INSERT, "Inició turno. " + nuevasAsignaciones.size() + " mascotas asignadas.");
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevasAsignaciones);
    }

    /**
     * Guarda el comentario de una mascota específica en el turno activo.
     */
    @Transactional
    @PostMapping("/guardar-comentario")
    public ResponseEntity<?> guardarComentario(@RequestBody Map<String, Object> payload,
                                               @RequestHeader("Authorization") String authHeader) {
        Usuario actor = getActorFromToken(authHeader);
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        Optional<Cuidador> cuidadorOpt = cuidadorService.findByUsuario(actor);
        if (cuidadorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Perfil de cuidador no encontrado."));
        }
        // ¡CORRECCIÓN! Usar ID Empleado
        Integer idEmpleado = cuidadorOpt.get().getIdEmpleado();

        Integer idMascota = (Integer) payload.get("idMascota");
        String comentarios = (String) payload.get("comentarios");

        if (idMascota == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "idMascota es requerido."));
        }

        // ¡CORRECCIÓN! Usar ID Empleado
        Optional<AsignacionCuidador> asignacionOpt = service.findActivaByIdMascotaAndIdEmpleado(idMascota, idEmpleado);
        if (asignacionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No se encontró una asignación activa para esta mascota."));
        }

        AsignacionCuidador asignacion = asignacionOpt.get();
        asignacion.setComentarios(comentarios);
        service.save(asignacion);

        return ResponseEntity.ok(asignacion);
    }

    /**
     * Cierra el turno activo del cuidador.
     */
    @Transactional
    @PostMapping("/terminar-turno")
    public ResponseEntity<?> terminarTurno(@RequestHeader("Authorization") String authHeader) {
        Usuario actor = getActorFromToken(authHeader);
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Token inválido."));
        }

        Optional<Cuidador> cuidadorOpt = cuidadorService.findByUsuario(actor);
        if (cuidadorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Perfil de cuidador no encontrado."));
        }
        // ¡CORRECCIÓN! Usar ID Empleado
        Integer idEmpleado = cuidadorOpt.get().getIdEmpleado();

        // ¡CORRECCIÓN! Usar ID Empleado
        List<AsignacionCuidador> turnoActivo = service.findActivasByIdEmpleado(idEmpleado);
        if (turnoActivo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "No tienes ningún turno activo para terminar."));
        }

        // ¡CORRECCIÓN! Usar ID Empleado
        service.terminarTurno(idEmpleado, LocalDate.now());

        registrarAuditoria(authHeader, "Asignacion_Cuidador", idEmpleado.toString(), Accion.UPDATE, "Terminó turno. " + turnoActivo.size() + " mascotas liberadas.");
        return ResponseEntity.ok(Map.of("message", "Turno terminado exitosamente."));
    }


    // --- HELPER DE AUDITORÍA ---
    private Usuario getActorFromToken(String authHeader) {
        try {
            String token = authHeader.substring(7); // Quita "Bearer "
            String login = jwtUtil.getLoginFromToken(token);
            Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
            return usuarioOpt.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Accion accion, String comentario) {
        Usuario actor = getActorFromToken(authHeader);

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