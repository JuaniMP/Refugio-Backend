package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Auditoria;
import co.edu.unbosque.veterinaria.entity.Auditoria.Accion;
import co.edu.unbosque.veterinaria.entity.Empleado;
import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Cuidador;
import co.edu.unbosque.veterinaria.entity.Veterinario;
import co.edu.unbosque.veterinaria.entity.Refugio;
import co.edu.unbosque.veterinaria.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.veterinaria.service.api.EmpleadoServiceAPI;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.CuidadorServiceAPI;
import co.edu.unbosque.veterinaria.service.api.VeterinarioServiceAPI;
import co.edu.unbosque.veterinaria.service.api.RefugioServiceAPI;
import co.edu.unbosque.veterinaria.utils.EmailService;
import co.edu.unbosque.veterinaria.utils.HashPass;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import co.edu.unbosque.veterinaria.utils.PasswordGenerator;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoRestController {

    // --- Dependencias existentes ---
    @Autowired private EmpleadoServiceAPI service;
    @Autowired private AuditoriaServiceAPI auditoriaService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UsuarioServiceAPI usuarioService;

    // --- Dependencias nuevas añadidas ---
    @Autowired private CuidadorServiceAPI cuidadorService;
    @Autowired private VeterinarioServiceAPI veterinarioService;
    @Autowired private PasswordGenerator passwordGenerator;
    @Autowired private HashPass hashPass;
    @Autowired private EmailService emailService;
    @Autowired private RefugioServiceAPI refugioService;

    // --- MÉTODOS EXISTENTES (SIN CAMBIOS) ---

    @GetMapping("/getAll")
    public List<Empleado> getAll() {
        return service.getAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Empleado> get(@PathVariable Integer id) throws ResourceNotFoundException {
        Empleado e = service.get(id);
        if (e == null) throw new ResourceNotFoundException("empleado no encontrado: " + id);
        return ResponseEntity.ok(e);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Empleado e,
                                  @RequestHeader("Authorization") String authHeader) {
        boolean esNuevo = (e.getIdEmpleado() == null);

        if (e.getRefugio() == null || e.getRefugio().getIdRefugio() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("debes enviar el refugio (idRefugio)");
        }
        if (!esNuevo) {
            Empleado existente = service.get(e.getIdEmpleado());
            if (existente == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("no existe el empleado con id " + e.getIdEmpleado());
            }
        }
        if (e.getCedula() != null && !e.getCedula().isBlank()) {
            String cedulaNorm = e.getCedula().trim();
            for (Empleado otro : service.getAll()) {
                if (otro.getCedula() != null && cedulaNorm.equalsIgnoreCase(otro.getCedula().trim())) {
                    if (esNuevo || !Objects.equals(otro.getIdEmpleado(), e.getIdEmpleado())) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("ya existe un empleado con la cedula " + cedulaNorm);
                    }
                }
            }
            e.setCedula(cedulaNorm);
        }
        if (e.getNombre() != null) e.setNombre(e.getNombre().trim());
        if (e.getTelefono() != null) e.setTelefono(e.getTelefono().trim());

        Empleado guardado;
        try {
            guardado = service.save(e);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se pudo guardar: posible cedula duplicada u otra restriccion");
        }

        String comentario = esNuevo
                ? "se creo empleado " + safeNombre(guardado) + " id=" + guardado.getIdEmpleado()
                : "se actualizo empleado " + safeNombre(guardado) + " id=" + guardado.getIdEmpleado();

        registrarAuditoria(
                authHeader,
                "Empleado",
                guardado.getIdEmpleado() != null ? guardado.getIdEmpleado().toString() : null,
                !esNuevo ? Accion.UPDATE : Accion.INSERT,
                comentario
        );

        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestHeader("Authorization") String authHeader) {
        Empleado e = service.get(id);
        if (e == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("no existe el empleado con id " + id);
        }
        try {
            service.delete(id);

            registrarAuditoria(
                    authHeader,
                    "Empleado",
                    id.toString(),
                    Accion.DELETE,
                    "se elimino el empleado " + safeNombre(e) + " id=" + id
            );

            return ResponseEntity.ok().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("no se puede eliminar: tiene subroles o referencias");
        }
    }

    private String safeNombre(Empleado e) {
        return (e.getNombre() == null || e.getNombre().isBlank()) ? "(sin nombre)" : e.getNombre();
    }

    private void registrarAuditoria(String authHeader, String tabla, String idRegistro, Accion accion, String comentario) {
        Usuario actor = null;
        try {
            String token = authHeader.substring(7);
            String login = jwtUtil.getLoginFromToken(token);
            Optional<Usuario> usuarioOpt = usuarioService.findByLogin(login);
            if (usuarioOpt.isPresent()) {
                actor = usuarioOpt.get();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario para auditoría: " + e.getMessage());
        }

        Auditoria aud = Auditoria.builder()
                .usuario(actor)
                .tablaAfectada(tabla)
                .idRegistro(idRegistro)
                .accion(accion)
                .comentarioAuditoria(comentario)
                .build();
        auditoriaService.save(aud);
    }


    // --- ⬇️ CÓDIGO NUEVO CORREGIDO ⬇️ ---

    @Transactional
    @PostMapping("/create-cuidador")
    public ResponseEntity<?> createCuidador(@RequestBody Map<String, Object> payload,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            // 1. Extraer datos
            String nombre = (String) payload.get("nombre");
            String cedula = (String) payload.get("cedula");
            String telefono = (String) payload.get("telefono");
            Integer idRefugio = (Integer) payload.get("idRefugio");
            String login = ((String) payload.get("login")).toLowerCase().trim();
            String turnoStr = (String) payload.get("turno");
            String zonaAsignada = (String) payload.get("zonaAsignada");

            if (usuarioService.findByLogin(login).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "El correo " + login + " ya está registrado."));
            }

            // 2. Crear Usuario
            String tempPassword = passwordGenerator.generateTemporaryPassword();
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setLogin(login);
            nuevoUsuario.setRol(Usuario.Rol.C); // Rol Cuidador
            nuevoUsuario.setEstado(Usuario.Estado.ACTIVO);
            nuevoUsuario.setRequiresPasswordChange(true);
            nuevoUsuario.setPasswordHash(hashPass.generarHash(nuevoUsuario, tempPassword));
            Usuario usuarioGuardado = usuarioService.save(nuevoUsuario);

            // 3. Crear Empleado
            Refugio refugio = refugioService.get(idRefugio);
            if (refugio == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El refugio con ID " + idRefugio + " no existe."));
            }
            Empleado nuevoEmpleado = new Empleado();
            nuevoEmpleado.setNombre(nombre);
            nuevoEmpleado.setCedula(cedula);
            nuevoEmpleado.setTelefono(telefono);
            nuevoEmpleado.setRefugio(refugio);
            nuevoEmpleado.setUsuario(usuarioGuardado);
            Empleado empleadoGuardado = service.save(nuevoEmpleado);

            // 4. Crear Cuidador (¡AQUÍ ESTÁ LA CORRECCIÓN!)
            Cuidador nuevoCuidador = new Cuidador();
            // NO se debe setear el ID manualmente con @MapsId
            // nuevoCuidador.setIdEmpleado(empleadoGuardado.getIdEmpleado()); // <--- LÍNEA INCORRECTA ELIMINADA
            nuevoCuidador.setEmpleado(empleadoGuardado); // <--- Esta línea es suficiente
            nuevoCuidador.setTurno(Cuidador.Turno.valueOf(turnoStr));
            nuevoCuidador.setZonaAsignada(zonaAsignada);
            cuidadorService.save(nuevoCuidador);

            // 5. Enviar Email
            emailService.sendTemporaryPasswordEmail(login, tempPassword);

            // 6. Auditoría
            registrarAuditoria(authHeader, "Empleado", empleadoGuardado.getIdEmpleado().toString(), Accion.INSERT, "Se creó Empleado (Cuidador): " + nombre);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Cuidador creado exitosamente."));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Error de integridad: La cédula o el email ya existen."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno: " + e.getMessage()));
        }
    }


    @Transactional
    @PostMapping("/create-vet")
    public ResponseEntity<?> createVet(@RequestBody Map<String, Object> payload,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            // 1. Extraer datos
            String nombre = (String) payload.get("nombre");
            String cedula = (String) payload.get("cedula");
            String telefono = (String) payload.get("telefono");
            Integer idRefugio = (Integer) payload.get("idRefugio");
            String login = ((String) payload.get("login")).toLowerCase().trim();
            String especialidad = (String) payload.get("especialidad");
            String registroProfesional = (String) payload.get("registroProfesional");

            if (usuarioService.findByLogin(login).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "El correo " + login + " ya está registrado."));
            }

            // 2. Crear Usuario
            String tempPassword = passwordGenerator.generateTemporaryPassword();
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setLogin(login);
            nuevoUsuario.setRol(Usuario.Rol.V); // Rol Veterinario
            nuevoUsuario.setEstado(Usuario.Estado.ACTIVO);
            nuevoUsuario.setRequiresPasswordChange(true);
            nuevoUsuario.setPasswordHash(hashPass.generarHash(nuevoUsuario, tempPassword));
            Usuario usuarioGuardado = usuarioService.save(nuevoUsuario);

            // 3. Crear Empleado
            Refugio refugio = refugioService.get(idRefugio);
            if (refugio == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "El refugio con ID " + idRefugio + " no existe."));
            }
            Empleado nuevoEmpleado = new Empleado();
            nuevoEmpleado.setNombre(nombre);
            nuevoEmpleado.setCedula(cedula);
            nuevoEmpleado.setTelefono(telefono);
            nuevoEmpleado.setRefugio(refugio);
            nuevoEmpleado.setUsuario(usuarioGuardado);
            Empleado empleadoGuardado = service.save(nuevoEmpleado);

            // 4. Crear Veterinario (¡AQUÍ ESTÁ LA CORRECCIÓN!)
            Veterinario nuevoVet = new Veterinario();
            // NO se debe setear el ID manualmente con @MapsId
            // nuevoVet.setIdEmpleado(empleadoGuardado.getIdEmpleado()); // <--- LÍNEA INCORRECTA ELIMINADA
            nuevoVet.setEmpleado(empleadoGuardado); // <--- Esta línea es suficiente
            nuevoVet.setEspecialidad(especialidad);
            nuevoVet.setRegistroProfesional(registroProfesional);
            veterinarioService.save(nuevoVet);

            // 5. Enviar Email
            emailService.sendTemporaryPasswordEmail(login, tempPassword);

            // 6. Auditoría
            registrarAuditoria(authHeader, "Empleado", empleadoGuardado.getIdEmpleado().toString(), Accion.INSERT, "Se creó Empleado (Veterinario): " + nombre);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Veterinario creado exitosamente."));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Error de integridad: La cédula o el email ya existen."));
        } catch (Exception e) {
            // Imprimir el error real en la consola del backend para depurar
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno: " + e.getMessage()));
        }
    }

}

// --- FIN DE LA CLASE EmpleadoRestController ---