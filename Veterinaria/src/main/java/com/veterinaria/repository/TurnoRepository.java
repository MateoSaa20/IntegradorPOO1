package com.veterinaria.repository;

import com.veterinaria.model.Turno;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TurnoRepository extends BaseRepository<Turno, Long> {

    public TurnoRepository(EntityManager em) {
        super(em, Turno.class);
    }
    /**
     * Busca todos los turnos asignados a un veterinario en una fecha específica.
     */
    public List<Turno> findByVeterinarioAndFecha(Long idVeterinario, LocalDate fecha) {
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();

        return em.createQuery(
            "SELECT t FROM Turno t WHERE t.veterinario.id = :vetId " +
            "AND t.fechaHora >= :inicioDia AND t.fechaHora < :finDia", Turno.class)
            .setParameter("vetId", idVeterinario)
            .setParameter("inicioDia", inicioDia)
            .setParameter("finDia", finDia)
            .getResultList();
    }

}