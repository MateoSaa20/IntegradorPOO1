package com.veterinaria.repository;

import com.veterinaria.model.TipoVacuna;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Optional;

public class TipoVacunaRepository extends BaseRepository<TipoVacuna, Long> {

    public TipoVacunaRepository(EntityManager em) {
        super(em, TipoVacuna.class);
    }

    /**
     * Busca la fecha de la última aplicación de un tipo de vacuna específico para una mascota.
     */
    public Optional<LocalDate> findUltimaFechaAplicacion(Long numeroFichaMascota, Long idTipoVacuna) {
        try {
            LocalDate fecha = em.createQuery(
                "SELECT MAX(CAST(t.fechaHora AS java.time.LocalDate)) " +
                "FROM DetalleVacunacion dv " +
                "JOIN dv.tipoVacuna tv " +
                "JOIN dv.itemTurno it " +
                "JOIN it.turno t " +
                "WHERE t.mascota.numeroFicha = :numeroFicha AND tv.idVacuna = :idVacuna", 
                LocalDate.class
            )
            .setParameter("numeroFicha", numeroFichaMascota)
            .setParameter("idVacuna", idTipoVacuna)
            .getSingleResult();

            return Optional.ofNullable(fecha);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}