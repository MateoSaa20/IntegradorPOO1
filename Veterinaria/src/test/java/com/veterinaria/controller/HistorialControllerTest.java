package com.veterinaria.controller;

import com.veterinaria.model.*;
import com.veterinaria.service.EstadoVacuna;
import com.veterinaria.service.HistorialService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regla de negocio del historial: el estado de cada vacuna se deriva de la
 * última aplicación (solo para vacunas cíclicas) y se alerta cuando falta
 * menos de un mes para la próxima dosis.
 */
public class HistorialControllerTest {

    private EntityManager em;
    private EntityManagerFactory emf;
    private HistorialController controller;
    private Mascota rocky;

    @BeforeEach
    public void setUp() {
        Map<String, String> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url",
                "jdbc:h2:mem:historial-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        props.put("hibernate.hbm2ddl.auto", "create-drop");

        emf = Persistence.createEntityManagerFactory("VeterinariaPU", props);
        em = emf.createEntityManager();
        controller = new HistorialController(new HistorialService(em));

        Especie canino = new Especie("Canino");
        Raza raza = new Raza("Mestizo", canino);
        Cliente cliente = new Cliente("María", "González", "30123456", "11-5555-1234");
        rocky = new Mascota("Rocky", LocalDate.now().minusYears(2), Sexo.MACHO, raza);
        rocky.setCliente(cliente);
        Veterinario carlos = new Veterinario("Carlos", "Gómez", "MP-1042");

        TipoVacuna rabisin = new TipoVacuna("Rabisin", "Rabia", 12, true);
        TipoVacuna quintuple = new TipoVacuna("Nobivac DHPPi", "Moquillo", 12, true);
        TipoVacuna unica = new TipoVacuna("Única Dosis", "Enfermedad X", 12, false);

        ServicioVacunacion servRabisin = new ServicioVacunacion("Vacuna Antirrábica", 9000, 15, rabisin);
        ServicioVacunacion servQuintuple = new ServicioVacunacion("Vacuna Quíntuple", 11500, 15, quintuple);
        ServicioVacunacion servUnica = new ServicioVacunacion("Vacuna Única", 5000, 15, unica);

        em.getTransaction().begin();
        em.persist(canino);
        em.persist(raza);
        em.persist(cliente);
        em.persist(rocky);
        em.persist(carlos);
        em.persist(rabisin);
        em.persist(quintuple);
        em.persist(unica);
        em.persist(servRabisin);
        em.persist(servQuintuple);
        em.persist(servUnica);
        em.getTransaction().commit();

        LocalDate hoy = LocalDate.now();

        // Rabisin hace ~11 meses y medio -> próxima en ~15 días (por vencer)
        vacunar(rocky, carlos, servRabisin, rabisin, hoy.minusMonths(11).minusDays(15));
        // Quíntuple hace ~13 meses -> VENCIDA
        vacunar(rocky, carlos, servQuintuple, quintuple, hoy.minusMonths(13).minusDays(15));
        // Única dosis hace 2 meses -> no cíclica, no corresponde alertar
        vacunar(rocky, carlos, servUnica, unica, hoy.minusMonths(2));
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

    // ==========================================================
    // BÚSQUEDA DEL DUEÑO POR DNI
    // ==========================================================

    @Test
    public void buscaClientePorDniConPuntos() {
        Optional<Cliente> resultado = controller.buscarClientePorDni("30.123.456");

        assertTrue(resultado.isPresent());
        assertEquals("María", resultado.get().getNombre());
    }

    @Test
    public void buscaClientePorDniSinPuntos() {
        assertTrue(controller.buscarClientePorDni("30123456").isPresent());
    }

    @Test
    public void dniInexistenteNoDevuelveCliente() {
        assertTrue(controller.buscarClientePorDni("99999999").isEmpty());
    }

    @Test
    public void dniVacioNoDevuelveCliente() {
        assertTrue(controller.buscarClientePorDni("   ").isEmpty());
    }

    // ==========================================================
    // ESTADO DE VACUNAS
    // ==========================================================

    @Test
    public void vacunaCiclicaConProximaEnUnMesEstaPorVencer() {
        List<EstadoVacuna> estados = controller.calcularEstadoVacunas(rocky);

        EstadoVacuna rabisin = estados.stream()
                .filter(e -> e.getNombreVacuna().equals("Rabisin"))
                .findFirst()
                .orElseThrow();

        assertTrue(rabisin.porVencer());
        assertTrue(rabisin.diasParaProxima() <= 30);
        assertEquals("Por vencer", rabisin.getEstado());
    }

    @Test
    public void vacunaCiclicaVencidaSeAlerta() {
        List<EstadoVacuna> estados = controller.calcularEstadoVacunas(rocky);

        EstadoVacuna quintuple = estados.stream()
                .filter(e -> e.getNombreVacuna().equals("Nobivac DHPPi"))
                .findFirst()
                .orElseThrow();

        assertTrue(quintuple.vencida());
        assertTrue(quintuple.porVencer());
        assertEquals("Vencida", quintuple.getEstado());
    }

    @Test
    public void vacunaNoCiclicaNoGeneraRecordatorio() {
        List<EstadoVacuna> estados = controller.calcularEstadoVacunas(rocky);

        EstadoVacuna unica = estados.stream()
                .filter(e -> e.getNombreVacuna().equals("Única Dosis"))
                .findFirst()
                .orElseThrow();

        assertNull(unica.proximaAplicacion());
        assertFalse(unica.porVencer());
        assertEquals("No aplica", unica.getEstado());
    }

    @Test
    public void vacunasPorVencerIncluyeSoloLasUrgentes() {
        List<EstadoVacuna> porVencer = controller.vacunasPorVencer(rocky);

        assertEquals(2, porVencer.size());
        assertTrue(porVencer.stream().allMatch(EstadoVacuna::porVencer));
    }

    @Test
    public void historalListaLasAtencionesAtendidas() {
        List<Turno> atenciones = controller.listarAtenciones(rocky);

        assertEquals(3, atenciones.size());
        assertTrue(atenciones.stream().allMatch(t -> t.getEstado() == EstadoTurno.ATENDIDO));
    }
}
