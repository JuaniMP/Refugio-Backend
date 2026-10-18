package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Especie;
import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.service.api.EspecieServiceAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/especies")
public class EspecieRestController extends CatalogoController<Especie> {

    public EspecieRestController(EspecieServiceAPI service, AuditoriaRegistrador auditoria) {
        super(service, auditoria);
    }

    @Override protected String tabla() { return "Especie"; }
    @Override protected String etiqueta() { return "la especie"; }
    @Override protected String motivoNoBorrable() { return "esta en uso"; }
}
