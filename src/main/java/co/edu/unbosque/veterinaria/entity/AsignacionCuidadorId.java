package co.edu.unbosque.veterinaria.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class AsignacionCuidadorId implements Serializable {

    private Integer idMascota;
    private Integer idEmpleado;
    private LocalDate fechaInicio;

    public AsignacionCuidadorId() { }

    public AsignacionCuidadorId(Integer idMascota, Integer idEmpleado, LocalDate fechaInicio) {
        this.idMascota = idMascota;
        this.idEmpleado = idEmpleado;
        this.fechaInicio = fechaInicio;
    }

    public Integer getIdMascota() { return idMascota; }
    public void setIdMascota(Integer idMascota) { this.idMascota = idMascota; }

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsignacionCuidadorId)) return false;
        AsignacionCuidadorId that = (AsignacionCuidadorId) o;
        return Objects.equals(idMascota, that.idMascota) &&
                Objects.equals(idEmpleado, that.idEmpleado) &&
                Objects.equals(fechaInicio, that.fechaInicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMascota, idEmpleado, fechaInicio);
    }
}


