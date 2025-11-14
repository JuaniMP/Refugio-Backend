package co.edu.unbosque.veterinaria.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Tomamos el email "from" desde application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía un correo de verificación de cuenta.
     */
    public void sendVerificationEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("¡Bienvenido a Bigotes Felices! Activa tu cuenta");
        message.setText("Gracias por registrarte en Bigotes Felices.\n\n" +
                "Tu código de verificación es: " + code + "\n\n" +
                "Este código expirará en 1 hora.\n\n" +
                "Si no te registraste, por favor ignora este correo.");

        mailSender.send(message);
    }

    /**
     * Envía un correo de reseteo de contraseña.
     */
    public void sendPasswordResetEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Bigotes Felices - Recuperación de Contraseña");
        message.setText("Has solicitado restablecer tu contraseña.\n\n" +
                "Tu código de recuperación es: " + code + "\n\n" +
                "Este código expirará en 15 minutos.\n\n" +
                "Si no solicitaste esto, por favor ignora este correo.");

        mailSender.send(message);
    }

    /**
     * Envía una contraseña temporal a un nuevo empleado (Vet/Cuidador).
     */
    public void sendTemporaryPasswordEmail(String toEmail, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Bienvenido a bordo - Tu cuenta de Bigotes Felices");
        message.setText("¡Hola!\n\n" +
                "Se ha creado una cuenta de empleado para ti en la plataforma de Bigotes Felices.\n\n" +
                "Tu correo de inicio de sesión es: " + toEmail + "\n" +
                "Tu contraseña temporal es: " + tempPassword + "\n\n" +
                "Por favor, inicia sesión y cambia tu contraseña inmediatamente.");

        mailSender.send(message);
    }
}