package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.HashPass;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Date;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired private UsuarioServiceAPI service;
    @Autowired private HashPass hashPass;

    @GetMapping("/getAll")
    public List<Usuario> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Usuario get(@PathVariable Long id) throws ResourceNotFoundException {
        Usuario u = service.get(id);
        if (u == null) throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        return u;
    }

    @PostMapping("/save")
    public ResponseEntity<Usuario> save(@RequestBody Usuario usuario) {
        boolean esNuevo = (usuario.getIdUsuario() == null);

        // Normalizar login antes del hash
        usuario.setLogin(usuario.getLogin().toLowerCase().trim());

        // Hashear la contraseña (sea insert o update)
        usuario.setPasswordHash(hashPass.generarHash(usuario, usuario.getPasswordHash()));

        // Estado por defecto si es nuevo
        if (esNuevo) {
            usuario.setEstado(Usuario.Estado.ACTIVO);
        }

        Usuario guardado = service.save(usuario);
        return ResponseEntity.ok(guardado);
    }


    // No DELETE: desactivar vía estado
}
