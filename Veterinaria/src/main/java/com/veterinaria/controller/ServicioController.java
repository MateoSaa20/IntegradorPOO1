package com.veterinaria.controller;

import com.veterinaria.model.Servicio;
import com.veterinaria.model.TipoVacuna;
import com.veterinaria.service.ServicioService;

import java.util.List;

/**
 * Orquesta las operaciones sobre los servicios (consulta, vacunación,
 * guardería y peluquería) aplicando las reglas de negocio delegadas a la
 * capa de servicios:
 * - El nombre es obligatorio y se normaliza a formato título.
 * - El precio y la duración deben ser mayores a cero.
 * - Un servicio de vacunación debe tener asociado un tipo de vacuna.
 * - Un servicio usado en algún turno no puede eliminarse.
 */
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    public void registrarServicio(Servicio servicio) {
        servicioService.registrarServicio(servicio);
    }

    public Servicio actualizar(Servicio servicio) {
        return servicioService.actualizarServicio(servicio);
    }

    public void eliminar(Long idServicio) {
        servicioService.eliminarServicio(idServicio);
    }

    public List<Servicio> listarTodos() {
        return servicioService.listarServicios();
    }

    public List<TipoVacuna> listarTiposVacuna() {
        return servicioService.listarTiposVacuna();
    }
}
