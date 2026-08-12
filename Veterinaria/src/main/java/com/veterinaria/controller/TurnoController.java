package com.veterinaria.controller;

import com.veterinaria.model.*;
import com.veterinaria.repository.*;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Orquesta las operaciones sobre turnos aplicando las reglas de negocio:
 * - La fecha/hora del turno debe ser futura.
 * - Un veterinario no puede tener turnos superpuestos (la guardería no
 *   cuenta en el cálculo de ocupación del veterinario).
 * - La guardería se cobra por cantidad de días y no puede iniciar en un
 *   día/hora anterior a la actual.
 * - Una vacuna no puede repetirse mientras esté dentro de su periodicidad.
 */
public class TurnoController {

    private final EntityManager em;
    private final TurnoRepository turnoRepository;
    private final TipoVacunaRepository tipoVacunaRepository;
    private final ClienteRepository clienteRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ServicioRepository servicioRepository;

    public TurnoController(EntityManager em) {
        this.em = em;
        this.turnoRepository = new TurnoRepository(em);
        this.tipoVacunaRepository = new TipoVacunaRepository(em);
        this.clienteRepository = new ClienteRepository(em);
        this.veterinarioRepository = new VeterinarioRepository(em);
        this.servicioRepository = new ServicioRepository(em);
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

        Turno turno = new Turno(
                fechaHora,
                EstadoTurno.PENDIENTE,
                veterinario,
                mascota
        );

        // 1. El turno debe ser para un día y hora futuros.
        turno.validarFechaFutura(LocalDateTime.now());

        boolean tieneGuarderia =
                servicios.stream()
                        .anyMatch(s -> s instanceof ServicioGuarderia);

        if (tieneGuarderia &&
                (ingresoGuarderia == null || salidaGuarderia == null)) {

            throw new Exception("Debe indicar el ingreso y la salida de la guardería.");
        }

        // Regla: el ingreso a la guardería es parte del turno, por lo que
        // no puede ser posterior al inicio del turno.
        if (tieneGuarderia &&
                ingresoGuarderia.isAfter(turno.getFechaHora())) {

            throw new Exception("El ingreso a la guardería no puede ser posterior al inicio del turno.");
        }

        // 2. Construir los items aplicando las reglas de cada servicio.
        for (Servicio servicio : servicios) {

            if (servicio instanceof ServicioGuarderia guarderia) {

                // Regla: la guardería se cobra por cantidad de días y no
                // puede iniciar en un día/hora anterior a la actual.
                ItemGuarderia itemGuarderia = new ItemGuarderia(
                        guarderia,
                        turno,
                        ingresoGuarderia,
                        salidaGuarderia
                );

                itemGuarderia.validarRango(LocalDateTime.now());

                // Regla: la guardería tiene un cupo máximo de animales en
                // simultáneo. Si el período elegido ya está completo, no se
                // puede agendar. El item actual aún no se persistió, por lo
                // que no se cuenta a sí mismo.
                long ocupadas = turnoRepository.contarGuarderiasOcupadas(
                        ingresoGuarderia,
                        salidaGuarderia
                );

                if (ocupadas >= guarderia.getCapacidadMaxima()) {
                    throw new Exception(
                            "La guardería está completa en ese período (cupo máximo: "
                                    + guarderia.getCapacidadMaxima()
                                    + " animales en simultáneo)."
                    );
                }

                turno.agregarItem(itemGuarderia);

            } else {

                if (servicio instanceof ServicioVacunacion) {

                    // Regla: no repetir una vacuna dentro de su periodicidad.
                    validarPeriodicidadVacuna(
                            mascota,
                            (ServicioVacunacion) servicio,
                            fechaHora
                    );
                }

                turno.agregarItem(new ItemTurno(servicio, turno));
            }
        }

        // 3. El veterinario no debe tener turnos superpuestos ese día
        //    (los ItemGuarderia no cuentan en el cálculo de ocupación).
        turno.validarDisponibilidad(
                turnoRepository.findByVeterinarioAndFecha(
                        veterinario.getIdVeterinario(),
                        fechaHora.toLocalDate()
                )
        );

        // 4. La mascota tampoco: no puede tener otro turno en el mismo
        //    horario con cualquier veterinario (aquí la guardería sí ocupa).
        turno.validarDisponibilidadMascota(
                turnoRepository.findByMascotaAndFecha(
                        mascota.getNumeroFicha(),
                        fechaHora.toLocalDate()
                )
        );

        // 5. Persistir.
        try {
            em.getTransaction().begin();

            turno.setMascota(em.merge(turno.getMascota()));
            turno.setVeterinario(em.merge(turno.getVeterinario()));

            for (ItemTurno item : turno.getItems()) {
                item.setServicio(em.merge(item.getServicio()));
            }

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
     * dentro de la ventana de periodicidad (meses), no se puede agendar.
     */
    private void validarPeriodicidadVacuna(Mascota mascota,
                                           ServicioVacunacion servicioVacunacion,
                                           LocalDateTime fechaTurno) throws Exception {

        TipoVacuna tipoVacuna = servicioVacunacion.getTipoVacuna();

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
                        fechaTurno.toLocalDate()
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

    // ==========================================================
    // ESTADOS
    // ==========================================================

    public void confirmarTurno(Long idTurno) throws Exception {

        Turno turno = turnoRepository.buscarPorId(idTurno)
                .orElseThrow(() -> new Exception("No se encontró el turno con ID: " + idTurno));

        try {
            em.getTransaction().begin();
            turno.confirmar();
            turnoRepository.actualizar(turno);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void cancelarTurno(Long idTurno) throws Exception {

        Turno turno = turnoRepository.buscarPorId(idTurno)
                .orElseThrow(() -> new Exception("No se encontró el turno con ID: " + idTurno));

        try {
            em.getTransaction().begin();
            turno.cancelar(LocalDateTime.now());
            turnoRepository.actualizar(turno);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    // ==========================================================
    // CONSULTAS
    // ==========================================================

    public List<Turno> listarTurnos() {
        return turnoRepository.buscarTodos();
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.buscarTodos();
    }

    public List<Veterinario> listarVeterinarios() {
        return veterinarioRepository.buscarTodos();
    }

    public List<Servicio> listarServicios() {
        return servicioRepository.buscarTodos();
    }

    public List<Mascota> listarMascotasDe(Cliente cliente) {

        if (cliente == null || cliente.getIdCliente() == null) {
            return List.of();
        }

        Cliente clienteCompleto =
                em.find(Cliente.class, cliente.getIdCliente());

        return clienteCompleto != null
                ? clienteCompleto.getMascotas()
                : List.of();
    }

    /**
     * Precio estimado de un servicio para el rango seleccionado en el
     * formulario. La guardería cobra por cantidad de días; el resto de los
     * servicios usan su precio base (no requieren rango de fechas).
     */
    public double calcularPrecioEstimado(Servicio servicio,
                                         LocalDateTime ingresoGuarderia,
                                         LocalDateTime salidaGuarderia) {

        if (servicio instanceof ServicioGuarderia guarderia) {
            return guarderia.calcularSubtotalPorDias(
                    ingresoGuarderia,
                    salidaGuarderia
            );
        }

        return servicio.calcularSubtotal();
    }

    /**
     * Total estimado para la vista: aplica la regla de cálculo de precio
     * de cada servicio (la guardería suma precio x cantidad de días).
     */
    public double calcularTotalEstimado(List<Servicio> servicios,
                                        LocalDateTime ingresoGuarderia,
                                        LocalDateTime salidaGuarderia) {

        return servicios.stream()
                .mapToDouble(s -> calcularPrecioEstimado(
                        s,
                        ingresoGuarderia,
                        salidaGuarderia
                ))
                .sum();
    }
}
