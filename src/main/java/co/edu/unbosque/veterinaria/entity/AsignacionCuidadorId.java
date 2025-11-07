package co.edu.unbosque.veterinaria.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class AsignacionCuidadorId implements Serializable {
    private Long idMascota;
    private Long idEmpleado;
    private LocalDate fechaInicio;

    public AsignacionCuidadorId() { }

    public AsignacionCuidadorId(Long idMascota, Long idEmpleado, LocalDate fechaInicio) {
        this.idMascota = idMascota;
        this.idEmpleado = idEmpleado;
        this.fechaInicio = fechaInicio;
    }

    public Long getIdMascota() { return idMascota; }
    public void setIdMascota(Long idMascota) { this.idMascota = idMascota; }
    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsignacionCuidadorId)) return false;
        AsignacionCuidadorId that = (AsignacionCuidadorId) o;
        return Objects.equals(idMascota, that.idMascota) &&
               Objects.equals(idEmpleado, that.idEmpleado) &&
               Objects.equals(fechaInicio, that.fechaInicio);
    }
    @Override public int hashCode() { return Objects.hash(idMascota, idEmpleado, fechaInicio); }
}
