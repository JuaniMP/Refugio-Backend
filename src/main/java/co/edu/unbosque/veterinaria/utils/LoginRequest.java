package co.edu.unbosque.veterinaria.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {

    @NotNull(message = "El login no puede ser nulo.")
    @NotBlank(message = "El login es obligatorio.")
    @JsonProperty("login")
    private String login;

    @NotNull(message = "La clave no puede ser nula.")
    @NotBlank(message = "La clave es obligatoria.")
    @JsonProperty("clave")
    private String clave;

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
}
