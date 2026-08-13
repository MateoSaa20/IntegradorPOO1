package com.veterinaria.repository;

import com.veterinaria.model.DetalleVacunacion;
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

    /**
     * Historial de atenciones de una mascota: turnos ya ATENDIDO, del más
     * reciente al más antiguo.
     */
    public List<Turno> listarTurnosAtendidosDeMascota(Long numeroFicha) {
        return em.createQuery(
                "SELECT t FROM Turno t " +
                "LEFT JOIN FETCH t.items it " +
                "LEFT JOIN FETCH it.detalleAtencion " +
                "WHERE t.mascota.numeroFicha = :ficha AND t.estado = :atendido " +
                "ORDER BY t.fechaHora DESC",
                Turno.class)
                .setParameter("ficha", numeroFicha)
                .setParameter("atendido", EstadoTurno.ATENDIDO)
                .getResultList();
    }

    /**
     * Vacunas aplicadas a una mascota a lo largo de su historial, del más
     * reciente al más antiguo. Se recorre Turno -> ItemTurno -> DetalleVacunacion.
     */
    public List<DetalleVacunacion> listarVacunasAplicadasDeMascota(Long numeroFicha) {
        return em.createQuery(
                "SELECT dv FROM DetalleVacunacion dv " +
                "JOIN FETCH dv.itemTurno it " +
                "JOIN it.turno t " +
                "WHERE t.mascota.numeroFicha = :ficha " +
                "ORDER BY t.fechaHora DESC",
                DetalleVacunacion.class)
                .setParameter("ficha", numeroFicha)
                .getResultList();
    }

}