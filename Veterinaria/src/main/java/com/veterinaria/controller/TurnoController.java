package com.veterinaria.controller;

import com.veterinaria.model.Cliente;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.Servicio;
import com.veterinaria.model.Turno;
import com.veterinaria.model.Veterinario;
import com.veterinaria.service.TurnoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Orquesta las operaciones sobre turnos delegando las reglas de negocio y
 * la persistencia a la capa de servicios:
 * - La fecha/hora del turno debe ser futura.
 * - Un veterinario no puede tener turnos superpuestos (la guardería no
 *   cuenta en el cálculo de ocupación del veterinario).
 * - La guardería se cobra por cantidad de días y no puede iniciar en un
 *   día/hora anterior a la actual.
 * - Una vacuna no puede repetirse mientras esté dentro de su periodicidad.
 */
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    // ==========================================================
    // AGENDAR
    // ==========================================================

    /**
     * Valida las reglas de negocio, construye el Turno con sus ItemTurno
     * (ItemGuarderia para el servicio de guardería) y lo persiste.
     */
    public Turno agendarTurno(LocalDateTime fechaHora,
                              Veterinario veterinario,
                              Mascota mascota,
                              List<Servicio> servicios,
                              LocalDateTime ingresoGuarderia,
                              LocalDateTime salidaGuarderia) throws Exception {

        return turnoService.agendarTurno(
                fechaHora,
                veterinario,
                mascota,
                servicios,
                ingresoGuarderia,
                salidaGuarderia
        );
    }

    // ==========================================================
    // ESTADOS
    // ==========================================================

    public void confirmarTurno(Long idTurno) throws Exception {
        turnoService.confirmarTurno(idTurno);
    }

    public void cancelarTurno(Long idTurno) throws Exception {
        turnoService.cancelarTurno(idTurno);
    }

    // ==========================================================
    // CONSULTAS
    // ==========================================================

    public List<Turno> listarTurnos() {
        return turnoService.listarTurnos();
    }

    public Optional<Cliente> buscarClientePorDni(String dni) {
        return turnoService.buscarClientePorDni(dni);
    }

    public List<Veterinario> listarVeterinarios() {
        return turnoService.listarVeterinarios();
    }

    public List<Servicio> listarServicios() {
        return turnoService.listarServicios();
    }

    public List<Mascota> listarMascotasDe(Cliente cliente) {
        return turnoService.listarMascotasDe(cliente);
    }

    /**
     * Precio estimado de un servicio para el rango seleccionado en el
     * formulario. La guardería cobra por cantidad de días; el resto de los
     * servicios usan su precio base (no requieren rango de fechas).
     */
    public double calcularPrecioEstimado(Servicio servicio,
                                         LocalDateTime ingresoGuarderia,
                                         LocalDateTime salidaGuarderia) {

        return turnoService.calcularPrecioEstimado(
                servicio,
                ingresoGuarderia,
                salidaGuarderia
        );
    }

    /**
     * Total estimado para la vista: aplica la regla de cálculo de precio
     * de cada servicio (la guardería suma precio x cantidad de días).
     */
    public double calcularTotalEstimado(List<Servicio> servicios,
                                        LocalDateTime ingresoGuarderia,
                                        LocalDateTime salidaGuarderia) {

        return turnoService.calcularTotalEstimado(
                servicios,
                ingresoGuarderia,
                salidaGuarderia
        );
    }
}
