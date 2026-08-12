package com.veterinaria.repository;

import com.veterinaria.model.EstadoTurno;
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
     * Busca los turnos que se encuentran en un estado determinado
     * (por ejemplo, solo los CONFIRMADO listos para atender).
     */
    public List<Turno> findByEstado(EstadoTurno estado) {
        return em.createQuery(
                "SELECT t FROM Turno t WHERE t.estado = :estado ORDER BY t.fechaHora",
                Turno.class)
                .setParameter("estado", estado)
                .getResultList();
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

    /**
     * Busca todos los turnos de una mascota (con cualquier veterinario)
     * en una fecha específica.
     */
    public List<Turno> findByMascotaAndFecha(Long numeroFicha, LocalDate fecha) {
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.plusDays(1).atStartOfDay();

        return em.createQuery(
            "SELECT t FROM Turno t WHERE t.mascota.numeroFicha = :ficha " +
            "AND t.fechaHora >= :inicioDia AND t.fechaHora < :finDia", Turno.class)
            .setParameter("ficha", numeroFicha)
            .setParameter("inicioDia", inicioDia)
            .setParameter("finDia", finDia)
            .getResultList();
    }

}