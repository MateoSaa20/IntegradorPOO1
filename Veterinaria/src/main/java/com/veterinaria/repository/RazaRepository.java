package com.veterinaria.repository;

import com.veterinaria.model.Raza;
import jakarta.persistence.EntityManager;
import java.util.List;

public class RazaRepository extends BaseRepository<Raza, Long> {

    public RazaRepository(EntityManager em) {
        super(em, Raza.class);
    }

    public List<Raza> buscarPorEspecie(Long idEspecie) {
        String jpql = "SELECT r FROM Raza r WHERE r.especie.idEspecie = :idEspecie";
        return em.createQuery(jpql, Raza.class)
                 .setParameter("idEspecie", idEspecie)
                 .getResultList();
    }
}