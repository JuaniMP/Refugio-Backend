package co.edu.unbosque.veterinaria.config;

import co.edu.unbosque.veterinaria.security.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Seguridad de la API (AuthInterceptor) y CORS en un solo lugar. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final String[] origenesPermitidos;

    public WebConfig(AuthInterceptor authInterceptor,
                     @Value("${cors.allowed-origins}") String[] origenesPermitidos) {
        this.authInterceptor = authInterceptor;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenesPermitidos)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
