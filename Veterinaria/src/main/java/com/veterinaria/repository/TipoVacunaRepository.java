package com.veterinaria.repository;

import com.veterinaria.model.TipoVacuna;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;

public class TipoVacunaRepository extends BaseRepository<TipoVacuna, Long> {

    public TipoVacunaRepository(EntityManager em) {
        super(em, TipoVacuna.class);
    }

    /**
     * Regla de negocio: un tipo de vacuna no puede eliminarse si está siendo
     * ofrecido como servicio de vacunación o si ya fue aplicado (queda el
     * antecedente para el cálculo de periodicidad).
     */
    public boolean tieneUso(Long idTipoVacuna) {
        Long enServicios = em.createQuery(
                "SELECT COUNT(sv) FROM ServicioVacunacion sv WHERE sv.tipoVacuna.id = :id",
                Long.class)
                .setParameter("id", idTipoVacuna)
                .getSingleResult();

        Long aplicada = em.createQuery(
                "SELECT COUNT(dv) FROM DetalleVacunacion dv WHERE dv.tipoVacuna.id = :id",
                Long.class)
                .setParameter("id", idTipoVacuna)
                .getSingleResult();

        return (enServicios != null && enServicios > 0)
                || (aplicada != null && aplicada > 0);
    }

    /**
     * Busca la fecha y hora de la última aplicación de un tipo de vacuna
     * específico para una mascota (a partir de los DetalleVacunacion
     * registrados al atender turnos).
     */
    public Optional<LocalDateTime> findUltimaFechaAplicacion(Long numeroFichaMascota, Long idTipoVacuna) {
        try {
            LocalDateTime fecha = em.createQuery(
                "SELECT MAX(t.fechaHora) " +
                "FROM DetalleVacunacion dv " +
                "JOIN dv.tipoVacuna tv " +
                "JOIN dv.itemTurno it " +
                "JOIN it.turno t " +
                "WHERE t.mascota.numeroFicha = :numeroFicha AND tv.id = :idTipoVacuna",
                LocalDateTime.class
            )
            .setParameter("numeroFicha", numeroFichaMascota)
            .setParameter("idTipoVacuna", idTipoVacuna)
            .getSingleResult();

            return Optional.ofNullable(fecha);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
