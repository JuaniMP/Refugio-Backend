package co.edu.unbosque.veterinaria.controller;

import co.edu.unbosque.veterinaria.entity.Raza;
import co.edu.unbosque.veterinaria.security.AuditoriaRegistrador;
import co.edu.unbosque.veterinaria.service.api.RazaServiceAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/razas")
public class RazaRestController extends CatalogoController<Raza> {

    public RazaRestController(RazaServiceAPI service, AuditoriaRegistrador auditoria) {
        super(service, auditoria);
    }

    @Override protected String tabla() { return "Raza"; }
    @Override protected String etiqueta() { return "la raza"; }
    @Override protected String motivoNoBorrable() { return "esta en uso por mascotas"; }

    @Override
    protected String validar(Raza raza) {
        return raza.getEspecie() == null || raza.getEspecie().getIdEspecie() == null
                ? "debes enviar la especie" : null;
    }

    /** El nombre de una raza solo tiene que ser único dentro de su especie. */
    @Override
    protected boolean mismoAmbito(Raza a, Raza b) {
        return b.getEspecie() != null
                && Objects.equals(a.getEspecie().getIdEspecie(), b.getEspecie().getIdEspecie());
    }
}
