package com.veterinaria.controller;

import com.veterinaria.model.Servicio;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.TipoVacuna;
import com.veterinaria.repository.ServicioRepository;
import com.veterinaria.repository.TipoVacunaRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Orquesta las operaciones sobre los servicios (consulta, vacunación,
 * guardería y peluquería) aplicando las reglas de negocio:
 * - El nombre es obligatorio y se normaliza a formato título.
 * - El precio y la duración deben ser mayores a cero.
 * - Un servicio de vacunación debe tener asociado un tipo de vacuna.
 * - Un servicio usado en algún turno no puede eliminarse.
 */
public class ServicioController {

    private final EntityManager em;
    private final ServicioRepository servicioRepository;
    private final TipoVacunaRepository tipoVacunaRepository;

    public ServicioController(EntityManager em) {
        this.em = em;
        this.servicioRepository = new ServicioRepository(em);
        this.tipoVacunaRepository = new TipoVacunaRepository(em);
    }

    public void registrarServicio(Servicio servicio) {
        servicio.validar();

        try {
            em.getTransaction().begin();
            if (servicio instanceof ServicioVacunacion vacunacion
                    && vacunacion.getTipoVacuna() != null) {
                vacunacion.setTipoVacuna(
                        em.merge(vacunacion.getTipoVacuna())
                );
            }
            servicioRepository.guardar(servicio);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public Servicio actualizar(Servicio servicio) {
        servicio.validar();

        try {
            em.getTransaction().begin();
            if (servicio instanceof ServicioVacunacion vacunacion
                    && vacunacion.getTipoVacuna() != null) {
                vacunacion.setTipoVacuna(
                        em.merge(vacunacion.getTipoVacuna())
                );
            }
            Servicio actualizado = servicioRepository.actualizar(servicio);
            em.getTransaction().commit();
            return actualizado;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void eliminar(Long idServicio) {
        if (idServicio == null || servicioRepository.tieneUso(idServicio)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un servicio que ya fue utilizado en un turno."
            );
        }

        try {
            em.getTransaction().begin();
            servicioRepository.buscarPorId(idServicio).ifPresent(servicio -> {
                servicioRepository.eliminar(servicio);
            });
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Servicio> listarTodos() {
        return servicioRepository.buscarTodos();
    }

    public List<TipoVacuna> listarTiposVacuna() {
        return tipoVacunaRepository.buscarTodos();
    }
}
