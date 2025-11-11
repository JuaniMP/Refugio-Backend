package co.edu.unbosque.veterinaria.entity;

import java.io.Serializable;
import java.util.Objects;

public class TelefonoRefugioId implements Serializable {

    private Integer idRefugio;
    private String telefono;

    public TelefonoRefugioId() { }

    public TelefonoRefugioId(Integer idRefugio, String telefono) {
        this.idRefugio = idRefugio;
        this.telefono = telefono;
    }

    public Integer getIdRefugio() { return idRefugio; }
    public void setIdRefugio(Integer idRefugio) { this.idRefugio = idRefugio; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TelefonoRefugioId)) return false;
        TelefonoRefugioId that = (TelefonoRefugioId) o;
        return Objects.equals(idRefugio, that.idRefugio) &&
                Objects.equals(telefono, that.telefono);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRefugio, telefono);
    }
}
