package co.edu.unbosque.veterinaria.utils;

import co.edu.unbosque.veterinaria.entity.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Hash de contraseñas con BCrypt (lento y con sal propia por contraseña).
 *
 * Las cuentas creadas antes usaban SHA-256(login + clave). Se siguen aceptando
 * y {@link #esLegado(Usuario)} indica cuándo conviene volver a guardarlas con BCrypt.
 */
@Component
public class HashPass {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public String hashear(String clave) {
        return bcrypt.encode(clave);
    }

    public boolean coincide(Usuario usuario, String clave) {
        String guardado = usuario.getPasswordHash();
        if (guardado == null || clave == null) return false;
        if (!esLegado(usuario)) return bcrypt.matches(clave, guardado);
        byte[] esperado = sha256Legado(usuario.getLogin(), clave).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperado, guardado.getBytes(StandardCharsets.UTF_8));
    }

    /** true si la contraseña sigue guardada con el formato SHA-256 antiguo. */
    public boolean esLegado(Usuario usuario) {
        String guardado = usuario.getPasswordHash();
        return guardado != null && !guardado.startsWith("$2");
    }

    private static String sha256Legado(String login, String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String texto = login.toLowerCase().trim() + clave;
            return HexFormat.of().formatHex(md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
