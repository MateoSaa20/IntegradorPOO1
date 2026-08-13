package com.veterinaria.controller;

import com.veterinaria.model.TipoVacuna;
import com.veterinaria.service.TipoVacunaService;

import java.util.List;

/**
 * Orquesta las operaciones sobre los tipos de vacuna aplicando las reglas
 * de negocio delegadas a la capa de servicios:
 * - El nombre comercial y la enfermedad que previene son obligatorios.
 * - La periodicidad debe ser mayor a cero.
 * - Un tipo de vacuna usado en un servicio o ya aplicado no puede eliminarse.
 */
public class TipoVacunaController {

    private final TipoVacunaService tipoVacunaService;

    public TipoVacunaController(TipoVacunaService tipoVacunaService) {
        this.tipoVacunaService = tipoVacunaService;
    }

    public void registrarTipoVacuna(TipoVacuna tipoVacuna) {
        tipoVacunaService.registrarTipoVacuna(tipoVacuna);
    }

    public TipoVacuna actualizar(TipoVacuna tipoVacuna) {
        return tipoVacunaService.actualizarTipoVacuna(tipoVacuna);
    }

    public void eliminar(Long idTipoVacuna) {
        tipoVacunaService.eliminarTipoVacuna(idTipoVacuna);
    }

    public List<TipoVacuna> listarTodos() {
        return tipoVacunaService.listarTiposVacuna();
    }
}
