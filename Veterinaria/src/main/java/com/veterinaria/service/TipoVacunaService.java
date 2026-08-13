package com.veterinaria.service;

import com.veterinaria.model.TipoVacuna;
import com.veterinaria.repository.TipoVacunaRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Coordina la persistencia y las reglas de negocio de los tipos de vacuna.
 * Los controladores no tocan el EntityManager directamente.
 */
public class TipoVacunaService {

    private final Transaccion transaccion;
    private final TipoVacunaRepository tipoVacunaRepository;

    public TipoVacunaService(EntityManager em) {
        this.transaccion = new Transaccion(em);
        this.tipoVacunaRepository = new TipoVacunaRepository(em);
    }

    public void registrarTipoVacuna(TipoVacuna tipoVacuna) {
        tipoVacuna.validar();

        try {
            transaccion.ejecutar(() -> tipoVacunaRepository.guardar(tipoVacuna));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al registrar el tipo de vacuna: " + e.getMessage(), e);
        }
    }

    public TipoVacuna actualizarTipoVacuna(TipoVacuna tipoVacuna) {
        tipoVacuna.validar();

        try {
            return transaccion.ejecutarConResultado(
                    () -> tipoVacunaRepository.actualizar(tipoVacuna));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al actualizar el tipo de vacuna: " + e.getMessage(), e);
        }
    }

    public void eliminarTipoVacuna(Long idTipoVacuna) {
        if (idTipoVacuna == null || tipoVacunaRepository.tieneUso(idTipoVacuna)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un tipo de vacuna que esté asociado a un servicio o ya haya sido aplicado."
            );
        }

        try {
            transaccion.ejecutar(() ->
                    tipoVacunaRepository.buscarPorId(idTipoVacuna)
                            .ifPresent(tipoVacunaRepository::eliminar));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al eliminar el tipo de vacuna: " + e.getMessage(), e);
        }
    }

    public List<TipoVacuna> listarTiposVacuna() {
        return tipoVacunaRepository.buscarTodos();
    }
}
