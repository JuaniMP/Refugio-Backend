package co.edu.unbosque.veterinaria.utils;

import co.edu.unbosque.veterinaria.entity.Usuario;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashPassTest {

    private final HashPass hashPass = new HashPass();

    @Test
    void lasClavesNuevasSeGuardanConBcrypt() {
        Usuario u = Usuario.builder().login("ana@correo.com").passwordHash(hashPass.hashear("Demo1234*")).build();
        assertThat(u.getPasswordHash()).startsWith("$2");
        assertThat(hashPass.esLegado(u)).isFalse();
        assertThat(hashPass.coincide(u, "Demo1234*")).isTrue();
        assertThat(hashPass.coincide(u, "otra")).isFalse();
    }

    @Test
    void sigueAceptandoLasCuentasAntiguasConSha256() {
        // SHA-256("ana@correo.com" + "Demo1234*"), formato usado antes de BCrypt
        Usuario u = Usuario.builder().login("Ana@Correo.com")
                .passwordHash("5b1f3ad26ba1903e9d53f3b0fe6bc1c7cef3ed0b7b7b170ef0e27059ce10f059").build();
        assertThat(hashPass.esLegado(u)).isTrue();
        assertThat(hashPass.coincide(u, "Demo1234*")).isTrue();
        assertThat(hashPass.coincide(u, "otra")).isFalse();
    }
}
