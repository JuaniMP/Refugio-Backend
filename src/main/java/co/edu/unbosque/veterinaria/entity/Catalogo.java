package co.edu.unbosque.veterinaria.entity;

/**
 * Entidad de catálogo administrable: tiene nombre único y estado ACTIVO/INACTIVO.
 * La implementan Especie, Raza y VacunaCatalogo para compartir CatalogoController.
 */
public interface Catalogo {

    Integer idCatalogo();

    String getNombre();

    void setNombre(String nombre);

    String getEstado();

    void setEstado(String estado);
}
