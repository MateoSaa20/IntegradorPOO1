package com.veterinaria.repository;

import com.veterinaria.model.Especialidad;
import jakarta.persistence.EntityManager;

public class EspecialidadRepository extends BaseRepository<Especialidad, Long> {

    public EspecialidadRepository(EntityManager em) {
        super(em, Especialidad.class);
    }
}
