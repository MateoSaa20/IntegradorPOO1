package com.veterinaria.repository;

import com.veterinaria.model.Especie;
import jakarta.persistence.EntityManager;
import java.util.List;

public class EspecieRepository extends BaseRepository<Especie, Long> {

    public EspecieRepository(EntityManager em) {
        super(em, Especie.class);
    }
    public List<Especie> buscarTodos() {
        return em.createQuery("SELECT e FROM Especie e", Especie.class).getResultList();
    }
}