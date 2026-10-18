package co.edu.unbosque.veterinaria.security;

import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Usuario.Rol;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Control de acceso de toda la API: valida el JWT y comprueba el rol según
 * las reglas de abajo. El administrador (AD) puede usar cualquier endpoint.
 * Las reglas se evalúan en orden y gana la primera que coincide.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** Atributo de request donde queda el usuario autenticado. */
    public static final String USUARIO = "usuarioAutenticado";

    private record Regla(String metodo, String patron, Set<Rol> roles) {
        boolean publica() { return roles == null; }
    }

    private static Regla publica(String metodo, String patron) {
        return new Regla(metodo, patron, null);
    }

    private static Regla para(String metodo, String patron, Rol... roles) {
        return new Regla(metodo, patron, roles.length == 0 ? EnumSet.allOf(Rol.class) : EnumSet.of(roles[0], roles));
    }

    private static final List<Regla> REGLAS = List.of(
            // Sin sesión: autenticación, registro y catálogo público de mascotas
            publica("POST", "/api/usuarios/login"),
            publica("POST", "/api/usuarios/verify-account"),
            publica("POST", "/api/usuarios/forgot-password"),
            publica("POST", "/api/usuarios/reset-password"),
            publica("POST", "/api/adoptantes/save"), // el controlador exige sesión para editar uno existente
            publica("GET", "/api/mascotas/getAll"),
            publica("GET", "/api/mascotas/{id:\\d+}"),
            publica("GET", "/api/especies/getAll"),
            publica("GET", "/api/razas/getAll"),
            publica("GET", "/api/refugios/getAll"),
            publica("GET", "/api/telefonos-refugio/getAll"),

            // Cualquier usuario con sesión
            para("POST", "/api/usuarios/force-reset-password"),

            // Adoptante
            para("GET", "/api/adoptantes/me", Rol.AP),
            para("POST", "/api/solicitudes/save", Rol.AP),
            para("GET", "/api/solicitudes/by-adoptante/me", Rol.AP),

            // Veterinario
            para("GET", "/api/veterinarios/me", Rol.V),
            para("*", "/api/historiales/**", Rol.V),
            para("*", "/api/diagnosticos/**", Rol.V),
            para("*", "/api/aplicaciones-vacuna/**", Rol.V),
            para("GET", "/api/vacunas/getAll", Rol.V),
            para("GET", "/api/mascotas/secure-search/**", Rol.V),

            // Cuidador
            para("GET", "/api/cuidadores/me", Rol.C),
            para("*", "/api/asignaciones/**", Rol.C),
            para("GET", "/api/mascotas/by-zona", Rol.C)
    );

    private final AntPathMatcher matcher = new AntPathMatcher();
    private final SesionActual sesionActual;

    public AuthInterceptor(SesionActual sesionActual) {
        this.sesionActual = sesionActual;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equals(request.getMethod())) return true; // preflight CORS

        String path = request.getRequestURI();
        Regla regla = REGLAS.stream()
                .filter(r -> ("*".equals(r.metodo()) || r.metodo().equals(request.getMethod()))
                        && matcher.match(r.patron(), path))
                .findFirst()
                .orElse(null);

        Usuario usuario = sesionActual.usuario(request.getHeader(HttpHeaders.AUTHORIZATION));
        request.setAttribute(USUARIO, usuario);
        if (regla != null && regla.publica()) return true;

        if (usuario == null || usuario.getEstado() != Usuario.Estado.ACTIVO) {
            return rechazar(response, HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión.");
        }
        boolean permitido = usuario.getRol() == Rol.AD || (regla != null && regla.roles().contains(usuario.getRol()));
        return permitido || rechazar(response, HttpServletResponse.SC_FORBIDDEN, "No tienes permisos para esta acción.");
    }

    private boolean rechazar(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + mensaje + "\"}");
        return false;
    }
}
