package com.veterinaria.controller;

import com.veterinaria.model.DetalleVacunacion;
import com.veterinaria.model.EstadoTurno;
import com.veterinaria.model.ItemTurno;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.TipoVacuna;
import com.veterinaria.model.Turno;
import com.veterinaria.model.Veterinario;
import com.veterinaria.repository.ServicioRepository;
import com.veterinaria.repository.TipoVacunaRepository;
import com.veterinaria.repository.VeterinarioRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Orquesta la pantalla de Control de Vacunaciones:
 * - Lista, para todas las mascotas, las vacunas vencidas o por vencer dentro
 *   del mes de aviso (reutiliza el cálculo de HistorialController/EstadoVacuna).
 * - Registra una nueva vacunación directamente sobre una mascota creando el
 *   turno ATENDIDO correspondiente. El estado de vigencia se recalcula en
 *   caliente desde la última aplicación, por lo que la alerta desaparece
 *   automáticamente al refrescar la lista.
 */
public class VacunacionController {

    private final EntityManager em;
    private final HistorialController historialController;
    private final TipoVacunaRepository tipoVacunaRepository;
    private final ServicioRepository servicioRepository;
    private final VeterinarioRepository veterinarioRepository;

    public VacunacionController(EntityManager em) {
        this.em = em;
        this.historialController = new HistorialController(em);
        this.tipoVacunaRepository = new TipoVacunaRepository(em);
        this.servicioRepository = new ServicioRepository(em);
        this.veterinarioRepository = new VeterinarioRepository(em);
    }

    // ==========================================================
    // ALERTAS GLOBALES
    // ==========================================================

    public List<AlertaVacunacion> listarVacunasEnAlerta() {
        List<Mascota> mascotas = em.createQuery(
                        "SELECT m FROM Mascota m",
                        Mascota.class)
                .getResultList();

        List<AlertaVacunacion> alertas = new ArrayList<>();
        for (Mascota mascota : mascotas) {
            for (EstadoVacuna estado : historialController.vacunasPorVencer(mascota)) {
                alertas.add(new AlertaVacunacion(mascota, estado));
            }
        }

        alertas.sort(
                Comparator.comparing((AlertaVacunacion a) -> a.estado().vencida()).reversed()
                        .thenComparing(a -> a.estado().diasParaProxima())
                        .thenComparing(AlertaVacunacion::getMascotaNombre)
        );

        return alertas;
    }

    // ==========================================================
    // CONSULTAS PARA EL FORMULARIO
    // ==========================================================

    public List<Mascota> listarMascotas() {
        return em.createQuery("SELECT m FROM Mascota m ORDER BY m.nombre", Mascota.class)
                .getResultList();
    }

    public List<ServicioVacunacion> listarServiciosVacunacion() {
        return servicioRepository.buscarTodos().stream()
                .filter(s -> s instanceof ServicioVacunacion)
                .map(s -> (ServicioVacunacion) s)
                .toList();
    }

    public List<Veterinario> listarVeterinarios() {
        return veterinarioRepository.buscarTodos();
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

        if (mascota == null || servicio == null || veterinario == null || fechaHora == null) {
            throw new Exception("Debe completar todos los campos para registrar la vacunación.");
        }

        validarPeriodicidadVacuna(mascota, servicio, fechaHora);

        Turno turno = new Turno(
                fechaHora,
                EstadoTurno.ATENDIDO,
                veterinario,
                mascota
        );

        ItemTurno item = new ItemTurno(servicio, turno);
        DetalleVacunacion detalle = new DetalleVacunacion(
                servicio.getTipoVacuna(),
                laboratorio,
                observacionesDosis
        );
        detalle.setObservaciones(
                "Vacunación " + servicio.getTipoVacuna().getNombreComercial()
        );
        detalle.validar();
        item.setDetalleAtencion(detalle);
        turno.agregarItem(item);

        try {
            em.getTransaction().begin();

            turno.setMascota(em.merge(turno.getMascota()));
            turno.setVeterinario(em.merge(turno.getVeterinario()));
            item.setServicio(em.merge(item.getServicio()));

            em.persist(turno);
            em.getTransaction().commit();

            return turno;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Regla de negocio de vacunación: si la mascota ya recibió esa vacuna
     * dentro de la ventana de periodicidad (meses), no se puede aplicar de
     * nuevo. Es el mismo criterio que usa TurnoController al agendar.
     */
    private void validarPeriodicidadVacuna(Mascota mascota,
                                           ServicioVacunacion servicio,
                                           LocalDateTime fechaAplicacion) throws Exception {

        TipoVacuna tipoVacuna = servicio.getTipoVacuna();

        if (tipoVacuna == null || mascota.getNumeroFicha() == null) {
            return;
        }

        Optional<LocalDateTime> ultimaAplicacion =
                tipoVacunaRepository.findUltimaFechaAplicacion(
                        mascota.getNumeroFicha(),
                        tipoVacuna.getId()
                );

        if (ultimaAplicacion.isPresent() &&
                tipoVacuna.estaDentroDePeriodicidad(
                        ultimaAplicacion.get().toLocalDate(),
                        fechaAplicacion.toLocalDate()
                )) {

            LocalDate proximaValida = ultimaAplicacion.get()
                    .toLocalDate()
                    .plusMonths(tipoVacuna.getPeriodicidadMeses());

            throw new Exception(
                    "La mascota ya recibió la vacuna "
                            + tipoVacuna.getNombreComercial()
                            + " el "
                            + ultimaAplicacion.get().toLocalDate()
                            + ". La periodicidad es de "
                            + tipoVacuna.getPeriodicidadMeses()
                            + " meses; la próxima aplicación será válida a partir del "
                            + proximaValida
                            + "."
            );
        }
    }
}
