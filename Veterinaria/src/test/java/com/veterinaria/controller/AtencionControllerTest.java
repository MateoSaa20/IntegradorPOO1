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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Regresión: al guardar o atender un turno cuyo item ya tiene un detalle
 * persistido, el detalle se reutiliza (UPDATE) en lugar de reemplazarse
 * por uno nuevo. Reemplazarlo insertaba una fila con el mismo id_item_turno
 * (clave única) antes de borrar la anterior y violaba la unicidad.
 */
public class AtencionControllerTest {

    private EntityManager em;
    private EntityManagerFactory emf;
    private AtencionController controller;
    private Turno turno;
    private Long itemId;

    @BeforeEach
    public void setUp() {
        Map<String, String> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url",
                "jdbc:h2:mem:atencion-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        props.put("hibernate.hbm2ddl.auto", "create-drop");

        emf = Persistence.createEntityManagerFactory("VeterinariaPU", props);
        em = emf.createEntityManager();
        controller = new AtencionController(em);

        Especie canino = new Especie("Canino");
        Raza raza = new Raza("Mestizo", canino);
        Cliente cliente = new Cliente("María", "González", "30123456", "11-5555-1234");
        Mascota rocky = new Mascota("Rocky", LocalDate.now().minusYears(2), Sexo.MACHO, raza);
        rocky.setCliente(cliente);
        Veterinario carlos = new Veterinario("Carlos", "Gómez", "MP-1042");
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 30);

        TipoVacuna rabisin = new TipoVacuna("Rabisin", "Rabia", 12, true);
        ServicioVacunacion vacunacion = new ServicioVacunacion("Vacuna Antirrábica", 9000, 15, rabisin);

        turno = new Turno(
                LocalDateTime.now().plusDays(1),
                EstadoTurno.CONFIRMADO,
                carlos,
                rocky
        );

        em.getTransaction().begin();
        em.persist(canino);
        em.persist(raza);
        em.persist(cliente);
        em.persist(rocky);
        em.persist(carlos);
        em.persist(rabisin);
        em.persist(consulta);
        em.persist(vacunacion);
        turno.agregarItem(new ItemTurno(consulta, turno));
        turno.agregarItem(new ItemTurno(vacunacion, turno));
        em.persist(turno);
        em.getTransaction().commit();

        itemId = turno.getItems().get(0).getIdItem();
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

    private long contarDetalles() {
        return em.createQuery("SELECT COUNT(d) FROM DetalleAtencion d", Long.class)
                .getSingleResult();
    }

    private DetalleAtencion detalleUnico() {
        List<DetalleAtencion> detalles =
                em.createQuery("SELECT d FROM DetalleAtencion d", DetalleAtencion.class)
                        .getResultList();
        return detalles.get(0);
    }

    @Test
    public void reemplazarDetalleDeConsultaNoViolaUnicidad() throws Exception {
        DetalleConsulta primero = new DetalleConsulta("Tranquilo", "Otitis");
        controller.guardarDetalles(turno.getIdTurno(), Map.of(itemId, primero));

        DetalleConsulta segundo = new DetalleConsulta("Inquieto", "Gingivitis");
        controller.guardarDetalles(turno.getIdTurno(), Map.of(itemId, segundo));

        assertEquals(1, contarDetalles());

        DetalleConsulta guardado = assertInstanceOf(
                DetalleConsulta.class,
                detalleUnico()
        );
        assertEquals("Gingivitis", guardado.getDiagnostico());
        assertEquals("Inquieto", guardado.getObservaciones());
    }

    @Test
    public void reemplazarDetalleDeVacunacionNoViolaUnicidad() throws Exception {
        Long idVacuna = turno.getItems().get(1).getIdItem();

        DetalleVacunacion primero =
                new DetalleVacunacion(null, "Zoetis", null);
        controller.guardarDetalles(turno.getIdTurno(), Map.of(idVacuna, primero));

        DetalleVacunacion segundo =
                new DetalleVacunacion(null, null, "1ra dosis");
        controller.guardarDetalles(turno.getIdTurno(), Map.of(idVacuna, segundo));

        assertEquals(1, contarDetalles());

        DetalleVacunacion guardado = assertInstanceOf(
                DetalleVacunacion.class,
                detalleUnico()
        );
        assertEquals("1ra dosis", guardado.getObservacionesDosis());
        assertEquals("Rabisin", guardado.getTipoVacuna().getNombreComercial());
    }

    @Test
    public void atenderTurnoConDetalleExistenteNoViolaUnicidad() throws Exception {
        DetalleConsulta primero = new DetalleConsulta("Tranquilo", "Otitis");
        controller.guardarDetalles(turno.getIdTurno(), Map.of(itemId, primero));

        DetalleConsulta alAtender = new DetalleConsulta("Final", "Curado");
        controller.atenderTurno(turno.getIdTurno(), Map.of(itemId, alAtender));

        assertEquals(1, contarDetalles());

        Turno atendido = em.find(Turno.class, turno.getIdTurno());
        assertEquals(EstadoTurno.ATENDIDO, atendido.getEstado());

        DetalleConsulta guardado = assertInstanceOf(
                DetalleConsulta.class,
                detalleUnico()
        );
        assertEquals("Curado", guardado.getDiagnostico());
    }
}
