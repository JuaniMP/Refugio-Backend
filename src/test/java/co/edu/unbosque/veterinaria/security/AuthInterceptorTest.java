package co.edu.unbosque.veterinaria.security;

import co.edu.unbosque.veterinaria.entity.Usuario;
import co.edu.unbosque.veterinaria.entity.Usuario.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {

    private final SesionActual sesion = mock(SesionActual.class);
    private final AuthInterceptor interceptor = new AuthInterceptor(sesion);

    private int estado(String metodo, String ruta, Rol rol) throws Exception {
        Usuario usuario = rol == null ? null : Usuario.builder().login("x@y.com").rol(rol).build();
        when(sesion.usuario(any())).thenReturn(usuario);
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean pasa = interceptor.preHandle(new MockHttpServletRequest(metodo, ruta), response, null);
        return pasa ? 200 : response.getStatus();
    }

    @Test
    void elCatalogoDeMascotasYElLoginSonPublicos() throws Exception {
        assertThat(estado("GET", "/api/mascotas/getAll", null)).isEqualTo(200);
        assertThat(estado("GET", "/api/mascotas/7", null)).isEqualTo(200);
        assertThat(estado("POST", "/api/usuarios/login", null)).isEqualTo(200);
    }

    @Test
    void sinSesionNoSePuedeListarUsuariosNiCrearlos() throws Exception {
        assertThat(estado("GET", "/api/usuarios/getAll", null)).isEqualTo(401);
        assertThat(estado("POST", "/api/usuarios/save", null)).isEqualTo(401);
    }

    @Test
    void cadaRolSoloAccedeASusEndpoints() throws Exception {
        assertThat(estado("POST", "/api/diagnosticos/save", Rol.V)).isEqualTo(200);
        assertThat(estado("POST", "/api/diagnosticos/save", Rol.AP)).isEqualTo(403);
        assertThat(estado("POST", "/api/asignaciones/comenzar-turno", Rol.C)).isEqualTo(200);
        assertThat(estado("POST", "/api/solicitudes/save", Rol.AP)).isEqualTo(200);
        assertThat(estado("GET", "/api/solicitudes/getAll", Rol.AP)).isEqualTo(403);
        assertThat(estado("DELETE", "/api/especies/1", Rol.V)).isEqualTo(403);
    }

    @Test
    void elAdministradorPuedeUsarCualquierEndpoint() throws Exception {
        assertThat(estado("GET", "/api/usuarios/getAll", Rol.AD)).isEqualTo(200);
        assertThat(estado("DELETE", "/api/especies/1", Rol.AD)).isEqualTo(200);
    }

    @Test
    void unaCuentaInactivaNoPuedeOperar() throws Exception {
        Usuario inactivo = Usuario.builder().login("x@y.com").rol(Rol.AD).estado(Usuario.Estado.INACTIVO).build();
        when(sesion.usuario(any())).thenReturn(inactivo);
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(new MockHttpServletRequest("GET", "/api/usuarios/getAll"), response, null)).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }
}
