package co.edu.unbosque.veterinaria.utils;

import org.springframework.stereotype.Component;
import co.edu.unbosque.veterinaria.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class HashPass {

    // 🔹 Genera hash SHA-256 combinando login normalizado + clave
    public String generarHash(Usuario usuario, String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            String loginNormalizado = usuario.getLogin().toLowerCase().trim();
            String texto = loginNormalizado + clave;

            byte[] hashBytes = md.digest(texto.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando hash", e);
        }
    }

    // 🔹 Sobrecarga: genera hash solo a partir de la clave
    public String generarHash(String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(clave.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando hash", e);
        }
    }

    // 🔹 Obtiene la IP real del cliente (por si te interesa dejarlo)
    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
