package com.veterinaria.controller;

import com.veterinaria.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regla de negocio: la guardería tiene un cupo máximo de animales en
 * simultáneo. Un turno con guardería que supere ese cupo debe rechazarse.
 */
public class GuarderiaCupoTest {

    private EntityManager em;
    private TurnoController controller;
    private Veterinario veterinario;
    private ServicioGuarderia guarderia;
    private Mascota m1;
    private Mascota m2;
    private Mascota m3;

    @BeforeEach
    public void setUp() {
        Map<String, String> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.url",
                "jdbc:h2:mem:guarderia-" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        props.put("hibernate.hbm2ddl.auto", "create-drop");

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("VeterinariaPU", props);
        em = emf.createEntityManager();

        Especie canino = new Especie("Canino");
        Raza raza = new Raza("Mestizo", canino);

        veterinario = new Veterinario("Carlos", "Gómez", "MP-1");
        guarderia = new ServicioGuarderia("Guardería Canina", 18000, 1440, 2);

        em.getTransaction().begin();
        em.persist(canino);
        em.persist(raza);
        em.persist(veterinario);
        em.persist(guarderia);
        m1 = mascota("Rocky", raza);
        m2 = mascota("Pelusa", raza);
        m3 = mascota("Nala", raza);
        em.getTransaction().commit();

        controller = new TurnoController(em);
    }

    private Mascota mascota(String nombre, Raza raza) {
        Cliente cliente = new Cliente(
                "Juan",
                "Perez",
                "DNI-" + nombre,
                "1111111111"
        );
        em.persist(cliente);

        Mascota mascota = new Mascota(
                nombre,
                LocalDate.now().minusYears(2),
                Sexo.MACHO,
                raza
        );
        mascota.setCliente(cliente);
        em.persist(mascota);
        return mascota;
    }

    private void agendarGuarderia(Mascota mascota, LocalDateTime ingreso, LocalDateTime salida) throws Exception {
        controller.agendarTurno(
                ingreso,
                veterinario,
                mascota,
                List.of(guarderia),
                ingreso,
                salida
        );
    }

    @Test
    public void permiteAgendarHastaCompletarElCupo() throws Exception {
        LocalDateTime ingreso = LocalDateTime.of(2099, 1, 10, 9, 0);
        LocalDateTime salida = LocalDateTime.of(2099, 1, 11, 9, 0);

        agendarGuarderia(m1, ingreso, salida);
        agendarGuarderia(m2, ingreso, salida);
    }

    @Test
    public void rechazaAgendarCuandoElCupoEstaCompleto() throws Exception {
        LocalDateTime ingreso = LocalDateTime.of(2099, 1, 10, 9, 0);
        LocalDateTime salida = LocalDateTime.of(2099, 1, 11, 9, 0);

        agendarGuarderia(m1, ingreso, salida);
        agendarGuarderia(m2, ingreso, salida);

        Exception exception = assertThrows(Exception.class,
                () -> agendarGuarderia(m3, ingreso, salida));

        assertTrue(exception.getMessage().contains("completa"));
    }

    @Test
    public void noBloqueaPeriodosQueNoSeSuperponen() throws Exception {
        agendarGuarderia(m1,
                LocalDateTime.of(2099, 1, 10, 9, 0),
                LocalDateTime.of(2099, 1, 11, 9, 0));

        agendarGuarderia(m2,
                LocalDateTime.of(2099, 1, 20, 9, 0),
                LocalDateTime.of(2099, 1, 21, 9, 0));
    }

    @Test
    public void solapamientoParcialCuentaEnElCupo() throws Exception {
        agendarGuarderia(m1,
                LocalDateTime.of(2099, 1, 10, 9, 0),
                LocalDateTime.of(2099, 1, 12, 9, 0));
        agendarGuarderia(m2,
                LocalDateTime.of(2099, 1, 11, 9, 0),
                LocalDateTime.of(2099, 1, 13, 9, 0));

        Exception exception = assertThrows(Exception.class,
                () -> agendarGuarderia(m3,
                        LocalDateTime.of(2099, 1, 11, 10, 0),
                        LocalDateTime.of(2099, 1, 12, 10, 0)));

        assertTrue(exception.getMessage().contains("completa"));
    }

    @Test
    public void turnoCanceladoLiberaUnLugarDeLaGuarderia() throws Exception {
        LocalDateTime ingreso = LocalDateTime.of(2099, 1, 10, 9, 0);
        LocalDateTime salida = LocalDateTime.of(2099, 1, 11, 9, 0);

        Turno turno1 = controller.agendarTurno(
                ingreso,
                veterinario,
                m1,
                List.of(guarderia),
                ingreso,
                salida
        );
        agendarGuarderia(m2, ingreso, salida);

        Exception exception = assertThrows(Exception.class,
                () -> agendarGuarderia(m3, ingreso, salida));
        assertTrue(exception.getMessage().contains("completa"));

        controller.cancelarTurno(turno1.getIdTurno());

        agendarGuarderia(m3, ingreso, salida);
        assertEquals(3, controller.listarTurnos().size());
    }
}
