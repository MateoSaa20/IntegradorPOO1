package com.veterinaria.repository;

import com.veterinaria.model.TipoVacuna;
import jakarta.persistence.EntityManager;

public class TipoVacunaRepository extends BaseRepository<TipoVacuna, Long> {

    public TipoVacunaRepository(EntityManager em) {
        super(em, TipoVacuna.class);
    }
}