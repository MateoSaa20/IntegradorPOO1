package com.veterinaria.controller;

import com.veterinaria.model.TipoVacuna;
import com.veterinaria.repository.TipoVacunaRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Orquesta las operaciones sobre los tipos de vacuna aplicando las reglas
 * de negocio:
 * - El nombre comercial y la enfermedad que previene son obligatorios.
 * - La periodicidad debe ser mayor a cero.
 * - Un tipo de vacuna usado en un servicio o ya aplicado no puede eliminarse.
 */
public class TipoVacunaController {

    private final EntityManager em;
    private final TipoVacunaRepository tipoVacunaRepository;

    public TipoVacunaController(EntityManager em) {
        this.em = em;
        this.tipoVacunaRepository = new TipoVacunaRepository(em);
    }

    public void registrarTipoVacuna(TipoVacuna tipoVacuna) {
        tipoVacuna.validar();

        try {
            em.getTransaction().begin();
            tipoVacunaRepository.guardar(tipoVacuna);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public TipoVacuna actualizar(TipoVacuna tipoVacuna) {
        tipoVacuna.validar();

        try {
            em.getTransaction().begin();
            TipoVacuna actualizado = tipoVacunaRepository.actualizar(tipoVacuna);
            em.getTransaction().commit();
            return actualizado;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void eliminar(Long idTipoVacuna) {
        if (idTipoVacuna == null || tipoVacunaRepository.tieneUso(idTipoVacuna)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un tipo de vacuna que esté asociado a un servicio o ya haya sido aplicado."
            );
        }

        try {
            em.getTransaction().begin();
            tipoVacunaRepository.buscarPorId(idTipoVacuna).ifPresent(tipoVacuna -> {
                tipoVacunaRepository.eliminar(tipoVacuna);
            });
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<TipoVacuna> listarTodos() {
        return tipoVacunaRepository.buscarTodos();
    }
}
