package co.edu.unbosque.veterinaria.security;

import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.service.api.UsuarioServiceAPI;
import co.edu.unbosque.veterinaria.utils.JwtUtil;
import org.springframework.stereotype.Component;

/**
 * Resuelve el usuario dueño de un header "Authorization: Bearer <jwt>".
 * Único lugar donde se interpreta el token (antes se repetía en cada controlador).
 */
@Component
public class SesionActual {

    private static final String BEARER = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UsuarioServiceAPI usuarioService;

    public SesionActual(JwtUtil jwtUtil, UsuarioServiceAPI usuarioService) {
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
    }

    /** Usuario del token, o null si no hay token, es inválido o el usuario ya no existe. */
    public Usuario usuario(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER)) return null;
        String token = authHeader.substring(BEARER.length());
        if (!jwtUtil.validateToken(token)) return null;
        return usuarioService.findByLogin(jwtUtil.getLoginFromToken(token)).orElse(null);
    }
}
