package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired private UsuarioServiceAPI service;

    @GetMapping("/getAll")
    public List<Usuario> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Usuario get(@PathVariable Long id) throws ResourceNotFoundException {
        Usuario u = service.get(id);
        if (u == null) throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        return u;
    }

    @PostMapping("/save")
    public Usuario save(@RequestBody Usuario u) { return service.save(u); }

    // No DELETE: desactivar vía estado
}
