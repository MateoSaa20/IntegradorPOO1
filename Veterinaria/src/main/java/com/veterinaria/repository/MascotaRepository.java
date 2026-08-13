package com.veterinaria.repository;

import com.veterinaria.model.Mascota;
import jakarta.persistence.EntityManager;

public class MascotaRepository extends BaseRepository<Mascota, Long> {

    public MascotaRepository(EntityManager em) {
        super(em, Mascota.class);
    }
}