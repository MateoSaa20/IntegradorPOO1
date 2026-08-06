package com.veterinaria.repository;

import com.veterinaria.model.Turno;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class TurnoRepository extends BaseRepository<Turno, Long> {

    public TurnoRepository(EntityManager em) {
        super(em, Turno.class);
    }

    public List<Turno> buscarPorVeterinarioYFecha(long idVeterinario, LocalDateTime inicio, LocalDateTime fin) {
        String jpql = "SELECT t FROM Turno t WHERE t.veterinario.id = :idVet AND t.fechaHora BETWEEN :inicio AND :fin";
        return em.createQuery(jpql, Turno.class)
                 .setParameter("idVet", idVeterinario)
                 .setParameter("inicio", inicio)
                 .setParameter("fin", fin)
                 .getResultList();
    }
}