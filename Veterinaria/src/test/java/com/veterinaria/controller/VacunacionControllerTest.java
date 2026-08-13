package com.veterinaria.controller;

import com.veterinaria.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas del control de vacunaciones: el listado de alertas debe incluir
 * solo vacunas vencidas o por vencer (dentro del mes de aviso) y, al
 * registrar una nueva aplicación, el estado de vigencia se recalcula y la
 * alerta desaparece. La periodicidad impide registrar aplicaciones repetidas
 * dentro de la ventana de meses.
 */
public class VacunacionControllerTest {

    private EntityManager em;
    private EntityManagerFactory emf;
    private VacunacionController controller;
    private Mascota rocky;
    private ServicioVacunacion servRabisin;
    private Veterinario carlos;

    @BeforeEach
    public void setUp() {
        Map<String, String> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url",
                "jdbc:h2:mem:vacunacion-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        props.put("hibernate.hbm2ddl.auto", "create-drop");

        emf = Persistence.createEntityManagerFactory("VeterinariaPU", props);
        em = emf.createEntityManager();
        controller = new VacunacionController(em);

        Especie canino = new Especie("Canino");
        Raza raza = new Raza("Mestizo", canino);
        Cliente cliente = new Cliente("María", "González", "30123456", "11-5555-1234");
        rocky = new Mascota("Rocky", LocalDate.now().minusYears(2), Sexo.MACHO, raza);
        rocky.setCliente(cliente);
        carlos = new Veterinario("Carlos", "Gómez", "MP-1042");

        TipoVacuna rabisin = new TipoVacuna("Rabisin", "Rabia", 12, true);
        servRabisin = new ServicioVacunacion("Vacuna Antirrábica", 9000, 15, rabisin);

        em.getTransaction().begin();
        em.persist(canino);
        em.persist(raza);
        em.persist(cliente);
        em.persist(rocky);
        em.persist(carlos);
        em.persist(rabisin);
        em.persist(servRabisin);
        em.getTransaction().commit();

        // Rabisin hace ~11 meses y medio -> próxima en ~15 días (por vencer)
        vacunar(rocky, carlos, servRabisin, rabisin, LocalDate.now().minusMonths(11).minusDays(15));
    }

    @AfterEach
    public void tearDown() {
        if (em != null) {
            em.close();
        }
        if (emf != null) {
            emf.close();
        }
    }

    private void vacunar(Mascota mascota,
                         Veterinario veterinario,
                         ServicioVacunacion servicio,
                         TipoVacuna tipo,
                         LocalDate fecha) {

        Turno turno = new Turno(
                fecha.atTime(10, 0),
                EstadoTurno.ATENDIDO,
                veterinario,
                mascota
        );
        ItemTurno item = new ItemTurno(servicio, turno);
        DetalleVacunacion detalle = new DetalleVacunacion(tipo, "Zoetis", "Refuerzo");
        item.setDetalleAtencion(detalle);
        turno.agregarItem(item);

        em.getTransaction().begin();
        em.persist(turno);
        em.getTransaction().commit();
    }

    private LocalDate primeraFechaValida() {
        return LocalDate.now().minusMonths(11).minusDays(15).plusMonths(12).plusDays(1);
    }

    // ==========================================================
    // LISTADO DE ALERTAS
    // ==========================================================

    @Test
    public void listarVacunasEnAlertaSoloDevuelveVencidasOPorVencer() {
        List<AlertaVacunacion> alertas = controller.listarVacunasEnAlerta();

        assertEquals(1, alertas.size());
        assertEquals("Rocky", alertas.get(0).getMascotaNombre());
        assertTrue(alertas.get(0).estado().porVencer());
        assertEquals("Por vencer", alertas.get(0).estado().getEstado());
    }

    @Test
    public void mascotaSinVacunasNoGeneraAlertas() {
        Mascota sola = new Mascota("Luna", LocalDate.now().minusYears(1), Sexo.HEMBRA,
                rocky.getRaza());
        Cliente cliente = rocky.getCliente();
        cliente.agregarMascota(sola);

        em.getTransaction().begin();
        em.persist(sola);
        em.getTransaction().commit();

        assertTrue(controller.listarVacunasEnAlerta().stream()
                .noneMatch(a -> a.getMascotaNombre().equals("Luna")));
    }

    // ==========================================================
    // REGISTRO DE UNA VACUNACIÓN
    // ==========================================================

    @Test
    public void registrarVacunacionDentroDePeriodicidadLanzaError() {
        LocalDate hoy = LocalDate.now();

        Exception ex = assertThrows(Exception.class,
                () -> controller.registrarVacunacion(
                        rocky, servRabisin, carlos, hoy.atTime(10, 0),
                        "Zoetis", "Refuerzo"));

        assertTrue(ex.getMessage().contains("La mascota ya recibió la vacuna"));
    }

    @Test
    public void registrarVacunacionEnFechaValidaQuitaLaAlerta() throws Exception {
        LocalDate nueva = primeraFechaValida();

        controller.registrarVacunacion(
                rocky, servRabisin, carlos, nueva.atTime(10, 0),
                "Zoetis", "Refuerzo");

        assertTrue(controller.listarVacunasEnAlerta().isEmpty());
    }

    @Test
    public void registrarVacunacionQuedaEnElHistorial() throws Exception {
        LocalDate nueva = primeraFechaValida();

        controller.registrarVacunacion(
                rocky, servRabisin, carlos, nueva.atTime(10, 0),
                "Zoetis", "Refuerzo");

        Long atendidos = em.createQuery(
                        "SELECT COUNT(t) FROM Turno t WHERE t.estado = :estado",
                        Long.class)
                .setParameter("estado", EstadoTurno.ATENDIDO)
                .getSingleResult();

        assertEquals(2, atendidos);
    }

    @Test
    public void registrarVacunacionExigeDatosCompletos() {
        assertThrows(Exception.class,
                () -> controller.registrarVacunacion(
                        null, servRabisin, carlos, LocalDateTime.now(),
                        "Zoetis", "Refuerzo"));
    }

    @Test
    public void listarServiciosVacunacionSoloIncluyeVacunas() {
        List<ServicioVacunacion> servicios = controller.listarServiciosVacunacion();

        assertEquals(1, servicios.size());
        assertEquals("Vacuna Antirrábica", servicios.get(0).getNombre());
    }
}
