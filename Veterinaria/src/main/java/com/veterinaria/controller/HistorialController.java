package com.veterinaria.controller;

import com.veterinaria.model.Cliente;
import com.veterinaria.model.DetalleVacunacion;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.Turno;
import com.veterinaria.service.EstadoVacuna;
import com.veterinaria.service.HistorialService;

import java.util.List;
import java.util.Optional;

/**
 * Orquesta la consulta del historial y de las vacunas de las mascotas
 * delegando la lógica a la capa de servicios:
 * - Busca al dueño por DNI.
 * - Lista sus mascotas, las atenciones realizadas y las vacunas aplicadas.
 * - Calcula el estado de cada vacuna cíclica (al día / por vencer / vencida)
 *   a partir de la última aplicación y la periodicidad del tipo de vacuna.
 */
public class HistorialController {

    private final HistorialService historialService;

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    public Optional<Cliente> buscarClientePorDni(String dni) {
        return historialService.buscarClientePorDni(dni);
    }

    public List<Mascota> listarMascotas(Cliente cliente) {
        return historialService.listarMascotas(cliente);
    }

    public List<Turno> listarAtenciones(Mascota mascota) {
        return historialService.listarAtenciones(mascota);
    }

    public List<DetalleVacunacion> listarVacunas(Mascota mascota) {
        return historialService.listarVacunas(mascota);
    }

    /**
     * Por cada tipo de vacuna aplicado a la mascota toma la última fecha de
     * aplicación y calcula la próxima dosis (solo para vacunas cíclicas).
     * Ordena primero las que requieren atención (por vencer/vencidas).
     */
    public List<EstadoVacuna> calcularEstadoVacunas(Mascota mascota) {
        return historialService.calcularEstadoVacunas(mascota);
    }

    public List<EstadoVacuna> vacunasPorVencer(Mascota mascota) {
        return historialService.vacunasPorVencer(mascota);
    }
}
