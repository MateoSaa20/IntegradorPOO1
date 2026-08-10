package com.veterinaria.repository;

import com.veterinaria.model.Especie;
import com.veterinaria.model.Raza;
import jakarta.persistence.EntityManager;
import java.util.List;

public class RazaRepository extends BaseRepository<Raza, Long> {

    public RazaRepository(EntityManager em) {
        super(em, Raza.class);
    }

    // 💡 Consulta JPQL filtrando por la relación de objeto
    public List<Raza> buscarPorEspecie(Especie especie) {
        return em.createQuery("SELECT r FROM Raza r WHERE r.especie = :especie", Raza.class)
                 .setParameter("especie", especie)
                 .getResultList();
    }
}