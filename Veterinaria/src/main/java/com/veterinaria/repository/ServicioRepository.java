package com.veterinaria.repository;

import com.veterinaria.model.Servicio;
import jakarta.persistence.EntityManager;

public class ServicioRepository extends BaseRepository<Servicio, Long> {

    public ServicioRepository(EntityManager em) {
        super(em, Servicio.class);
    }

    /**
     * Regla de negocio: un servicio que ya fue usado en un turno (ItemTurno,
     * incluida la guardería) no puede eliminarse porque se conserva el valor
     * histórico del mismo en cada turno atendido.
     */
    public boolean tieneUso(Long idServicio) {
        Long cantidad = em.createQuery(
                "SELECT COUNT(it) FROM ItemTurno it WHERE it.servicio.id = :idServicio",
                Long.class)
                .setParameter("idServicio", idServicio)
                .getSingleResult();

        return cantidad != null && cantidad > 0;
    }
}
