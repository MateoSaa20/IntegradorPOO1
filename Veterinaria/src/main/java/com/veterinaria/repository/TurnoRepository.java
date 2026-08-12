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
     * Cuenta cuántas mascotas están en la guardería en algún punto del
     * período [inicio, fin]. Solo considera turnos no CANCELADOS y cuenta
     * las estadías cuyo rango se solapa con el período consultado.
     */
    public long contarGuarderiasOcupadas(LocalDateTime inicio, LocalDateTime fin) {
        return em.createQuery(
                "SELECT COUNT(i) FROM ItemGuarderia i " +
                "WHERE i.turno.estado <> :cancelado " +
                "AND i.fechaHoraInicio < :fin " +
                "AND i.fechaHoraFin > :inicio",
                Long.class)
                .setParameter("cancelado", EstadoTurno.CANCELADO)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getSingleResult();
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