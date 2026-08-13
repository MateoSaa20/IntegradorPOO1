package com.veterinaria.repository;

import com.veterinaria.model.Mascota;
import jakarta.persistence.EntityManager;

import java.util.List;

public class MascotaRepository extends BaseRepository<Mascota, Long> {

    public MascotaRepository(EntityManager em) {
        super(em, Mascota.class);
    }

    public List<Mascota> buscarTodasOrdenadasPorNombre() {
        return em.createQuery(
                        "SELECT m FROM Mascota m ORDER BY m.nombre",
                        Mascota.class)
                .getResultList();
    }
}