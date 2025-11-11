package co.edu.unbosque.veterinaria.service.impl;

import co.edu.unbosque.veterinaria.entity.Adopcion;
import co.edu.unbosque.veterinaria.entity.Mascota;
import co.edu.unbosque.veterinaria.entity.SolicitudAdopcion;
import co.edu.unbosque.veterinaria.repository.AdopcionRepository;
import co.edu.unbosque.veterinaria.repository.MascotaRepository;
import co.edu.unbosque.veterinaria.repository.SolicitudAdopcionRepository;
import co.edu.unbosque.veterinaria.service.api.SolicitudAdopcionServiceAPI;
import co.edu.unbosque.veterinaria.utils.GenericServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SolicitudAdopcionServiceImpl
        extends GenericServiceImpl<SolicitudAdopcion, Integer>
        implements SolicitudAdopcionServiceAPI {

    @Autowired private SolicitudAdopcionRepository repo;
    @Autowired private AdopcionRepository adopcionRepo;
    @Autowired private MascotaRepository mascotaRepo;

    // Debe ser public y devolver un JpaRepository
    @Override
    public JpaRepository<SolicitudAdopcion, Integer> getDao() {
        return repo;
    }

    @Override
    @Transactional
    public SolicitudAdopcion aprobarYGenerarAdopcion(Integer idSolicitud) {
        // 1) Traer solicitud
        SolicitudAdopcion s = repo.findById(idSolicitud).orElseThrow(() ->
                new IllegalArgumentException("no existe la solicitud " + idSolicitud));

        // 2) Validar estado
        if (s.getEstado() == SolicitudAdopcion.Estado.APROBADA
                || s.getEstado() == SolicitudAdopcion.Estado.RECHAZADA
                || s.getEstado() == SolicitudAdopcion.Estado.CANCELADA) {
            throw new IllegalStateException("la solicitud ya fue procesada (" + s.getEstado() + ")");
        }

        // 3) Validar mascota
        Mascota m = s.getMascota();
        if (m == null || m.getIdMascota() == null) {
            throw new IllegalStateException("la solicitud no tiene mascota asociada");
        }

        // 4) Crear adopción (coincidiendo con tu tabla: fecha_adopcion)
        Adopcion adop = Adopcion.builder()
                .adoptante(s.getAdoptante())
                .mascota(m)
                // usa el nombre REAL del campo fecha en tu entidad Adopcion:
                .fechaAdopcion(LocalDate.now())
                .build();
        adopcionRepo.save(adop);

        // 5) Actualizar solicitud y mascota
        s.setEstado(SolicitudAdopcion.Estado.APROBADA); // enum, no String
        repo.save(s);

        // Mascota.estado también es enum (EN_REFUGIO, EN_PROCESO_ADOPCION, ADOPTADA, OTRO)
        m.setEstado(Mascota.Estado.ADOPTADA);
        mascotaRepo.save(m);

        return s;
    }
}
