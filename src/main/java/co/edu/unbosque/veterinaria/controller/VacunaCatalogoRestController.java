package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.VacunaCatalogo;
import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.service.api.VacunaCatalogoServiceAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vacunas")
public class VacunaCatalogoRestController extends CatalogoController<VacunaCatalogo> {

    public VacunaCatalogoRestController(VacunaCatalogoServiceAPI service, AuditoriaRegistrador auditoria) {
        super(service, auditoria);
    }

    @Override protected String tabla() { return "Vacuna_Catalogo"; }
    @Override protected String etiqueta() { return "la vacuna"; }
    @Override protected String motivoNoBorrable() { return "ya fue aplicada"; }
}
