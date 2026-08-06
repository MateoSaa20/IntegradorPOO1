package com.veterinaria.repository;

import com.veterinaria.model.Servicio;
import jakarta.persistence.EntityManager;

public class ServicioRepository extends BaseRepository<Servicio, Long> {

    public ServicioRepository(EntityManager em) {
        super(em, Servicio.class);
    }
}
