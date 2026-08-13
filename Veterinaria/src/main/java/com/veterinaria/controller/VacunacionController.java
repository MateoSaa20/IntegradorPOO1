package com.veterinaria.controller;

import com.veterinaria.model.Mascota;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.Turno;
import com.veterinaria.model.Veterinario;
import com.veterinaria.service.AlertaVacunacion;
import com.veterinaria.service.VacunacionService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquesta la pantalla de Control de Vacunaciones delegando la lógica a la
 * capa de servicios:
 * - Lista, para todas las mascotas, las vacunas vencidas o por vencer dentro
 *   del mes de aviso (reutiliza el cálculo de HistorialService/EstadoVacuna).
 * - Registra una nueva vacunación directamente sobre una mascota creando el
 *   turno ATENDIDO correspondiente. El estado de vigencia se recalcula en
 *   caliente desde la última aplicación, por lo que la alerta desaparece
 *   automáticamente al refrescar la lista.
 */
public class VacunacionController {

    private final VacunacionService vacunacionService;

    public VacunacionController(VacunacionService vacunacionService) {
        this.vacunacionService = vacunacionService;
    }

    // ==========================================================
    // ALERTAS GLOBALES
    // ==========================================================

    public List<AlertaVacunacion> listarVacunasEnAlerta() {
        return vacunacionService.listarVacunasEnAlerta();
    }

    // ==========================================================
    // CONSULTAS PARA EL FORMULARIO
    // ==========================================================

    public List<Mascota> listarMascotas() {
        return vacunacionService.listarMascotas();
    }

    public List<ServicioVacunacion> listarServiciosVacunacion() {
        return vacunacionService.listarServiciosVacunacion();
    }

    public List<Veterinario> listarVeterinarios() {
        return vacunacionService.listarVeterinarios();
    }

    // ==========================================================
    // REGISTRAR VACUNACIÓN
    // ==========================================================

    /**
     * Registra una vacunación aplicada sobre la mascota creando el turno
     * ATENDIDO correspondiente (mismo patrón que los datos de ejemplo).
     * Aplica la regla de periodicidad: no se puede repetir una vacuna
     * mientras esté dentro de su ventana de meses.
     */
    public Turno registrarVacunacion(Mascota mascota,
                                     ServicioVacunacion servicio,
                                     Veterinario veterinario,
                                     LocalDateTime fechaHora,
                                     String laboratorio,
                                     String observacionesDosis) throws Exception {

        return vacunacionService.registrarVacunacion(
                mascota,
                servicio,
                veterinario,
                fechaHora,
                laboratorio,
                observacionesDosis
        );
    }
}
