package com.veterinaria.repository;

import com.veterinaria.model.Especie;
import com.veterinaria.model.Raza;
import jakarta.persistence.EntityManager;
import java.util.List;

public class EspecieRepository extends BaseRepository<Especie, Long> {

    public EspecieRepository(EntityManager em) {
        super(em, Especie.class);
    }
}