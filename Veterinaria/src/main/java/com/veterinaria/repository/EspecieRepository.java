package com.veterinaria.repository;

import com.veterinaria.model.Especie;
import jakarta.persistence.EntityManager;

public class EspecieRepository extends BaseRepository<Especie, Long> {

    public EspecieRepository(EntityManager em) {
        super(em, Especie.class);
    }
}