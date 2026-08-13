package com.veterinaria.config;

import com.veterinaria.model.Cliente;
import com.veterinaria.model.DetalleAtencion;
import com.veterinaria.model.DetalleConsulta;
import com.veterinaria.model.DetalleVacunacion;
import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Especie;
import com.veterinaria.model.EstadoTurno;
import com.veterinaria.model.ItemTurno;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.Raza;
import com.veterinaria.model.Servicio;
import com.veterinaria.model.ServicioConsulta;
import com.veterinaria.model.ServicioGuarderia;
import com.veterinaria.model.ServicioPeluqueria;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.Sexo;
import com.veterinaria.model.TipoVacuna;
import com.veterinaria.model.Tratamiento;
import com.veterinaria.model.Turno;
import com.veterinaria.model.Veterinario;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;

/**
 * Carga los datos iniciales de la aplicación.
 * Es idempotente: cada sección solo inserta si la tabla correspondiente
 * está vacía, por lo que puede ejecutarse en cada inicio sin duplicar datos.
 */
public class DataInitializer {

    private DataInitializer() {
    }

    public static void cargarDatosIniciales(EntityManager em) {
        cargarEspeciesYrazas(em);
        cargarEspecialidadesYVeterinarios(em);
        cargarTiposDeVacunaYServicios(em);
        cargarClientesYMascotas(em);
        cargarTurnosYVacunasEjemplo(em);
    }

    // ==========================================================
    // ESPECIES Y RAZAS
    // ==========================================================

    private static void cargarEspeciesYrazas(EntityManager em) {
        if (hayDatos(em, "SELECT COUNT(r) FROM Raza r")) {
            return;
        }

        em.getTransaction().begin();
        try {
            Especie canino = new Especie("Canino");
            Especie felino = new Especie("Felino");
            em.persist(canino);
            em.persist(felino);

            em.persist(new Raza("Mestizo", canino));
            em.persist(new Raza("Labrador", canino));
            em.persist(new Raza("Caniche", canino));
            em.persist(new Raza("Ovejero Alemán", canino));

            em.persist(new Raza("Mestizo Felino", felino));
            em.persist(new Raza("Siamés", felino));
            em.persist(new Raza("Persa", felino));

            em.getTransaction().commit();
            System.out.println("✅ Especies y razas cargadas correctamente.");
        } catch (Exception e) {
            deshacer(em, "Error al cargar especies y razas.", e);
        }
    }

    // ==========================================================
    // ESPECIALIDADES Y VETERINARIOS
    // ==========================================================

    private static void cargarEspecialidadesYVeterinarios(EntityManager em) {
        if (hayDatos(em, "SELECT COUNT(v) FROM Veterinario v")) {
            return;
        }

        em.getTransaction().begin();
        try {
            Especialidad clinicaGeneral = new Especialidad(
                    "Clínica General",
                    "Atención médica integral de mascotas"
            );
            Especialidad cirugia = new Especialidad(
                    "Cirugía",
                    "Cirugías generales y especializadas"
            );
            Especialidad dermatologia = new Especialidad(
                    "Dermatología",
                    "Tratamiento de enfermedades de la piel"
            );
            em.persist(clinicaGeneral);
            em.persist(cirugia);
            em.persist(dermatologia);

            Veterinario carlos = new Veterinario("Carlos", "Gómez", "MP-1042");
            carlos.agregarEspecialidad(clinicaGeneral);
            carlos.agregarEspecialidad(cirugia);

            Veterinario laura = new Veterinario("Laura", "Martínez", "MP-2085");
            laura.agregarEspecialidad(clinicaGeneral);
            laura.agregarEspecialidad(dermatologia);

            Veterinario jorge = new Veterinario("Jorge", "Ramírez", "MP-3150");
            jorge.agregarEspecialidad(cirugia);

            em.persist(carlos);
            em.persist(laura);
            em.persist(jorge);

            em.getTransaction().commit();
            System.out.println("✅ Especialidades y veterinarios cargados correctamente.");
        } catch (Exception e) {
            deshacer(em, "Error al cargar especialidades y veterinarios.", e);
        }
    }

    // ==========================================================
    // TIPOS DE VACUNA Y SERVICIOS
    // ==========================================================

    private static void cargarTiposDeVacunaYServicios(EntityManager em) {
        if (hayDatos(em, "SELECT COUNT(s) FROM Servicio s")) {
            return;
        }

        em.getTransaction().begin();
        try {
            TipoVacuna antirrabica = new TipoVacuna(
                    "Rabisin",
                    "Rabia",
                    12,
                    true
            );
            TipoVacuna quintuple = new TipoVacuna(
                    "Nobivac DHPPi",
                    "Moquillo, Parvovirus, Hepatitis, Leptospirosis",
                    12,
                    true
            );
            em.persist(antirrabica);
            em.persist(quintuple);

            // Se fuerza la sincronización para obtener los IDs generados
            // antes de asociarlos a los servicios de vacunación.
            em.flush();

            ServicioConsulta consulta = new ServicioConsulta(
                    "Consulta Clínica General",
                    6500.0,
                    30
            );
            ServicioVacunacion servicioAntirrabica = new ServicioVacunacion(
                    "Vacuna Antirrábica",
                    9000.0,
                    15,
                    antirrabica
            );
            ServicioVacunacion servicioQuintuple = new ServicioVacunacion(
                    "Vacuna Quíntuple",
                    11500.0,
                    15,
                    quintuple
            );
            ServicioPeluqueria peluqueria = new ServicioPeluqueria(
                    "Peluquería y Baño Completo",
                    14000.0,
                    60
            );
            ServicioGuarderia guarderia = new ServicioGuarderia(
                    "Guardería Canina/Felina (por Día)",
                    18000.0,
                    1440,
                    ServicioGuarderia.CAPACIDAD_DEFECTO
            );

            em.persist(consulta);
            em.persist(servicioAntirrabica);
            em.persist(servicioQuintuple);
            em.persist(peluqueria);
            em.persist(guarderia);

            em.getTransaction().commit();
            System.out.println("✅ Tipos de vacuna y servicios cargados correctamente.");
        } catch (Exception e) {
            deshacer(em, "Error al cargar tipos de vacuna y servicios.", e);
        }
    }

    // ==========================================================
    // CLIENTES Y MASCOTAS (datos de ejemplo)
    // ==========================================================

    private static void cargarClientesYMascotas(EntityManager em) {
        if (hayDatos(em, "SELECT COUNT(c) FROM Cliente c")) {
            return;
        }

        em.getTransaction().begin();
        try {
            Raza mestizo = razaPorNombre(em, "Mestizo");
            Raza mestizoFelino = razaPorNombre(em, "Mestizo Felino");
            Raza caniche = razaPorNombre(em, "Caniche");
            Raza labrador = razaPorNombre(em, "Labrador");
            Raza ovejero = razaPorNombre(em, "Ovejero Alemán");
            Raza siames = razaPorNombre(em, "Siamés");
            Raza persa = razaPorNombre(em, "Persa");

            Cliente maria = new Cliente(
                    "María",
                    "González",
                    "30123456",
                    "11-5555-1234"
            );
            em.persist(maria);

            Mascota rocky = new Mascota(
                    "Rocky",
                    LocalDate.of(2023, 3, 12),
                    Sexo.MACHO,
                    mestizo
            );
            Mascota luna = new Mascota(
                    "Luna",
                    LocalDate.of(2024, 7, 1),
                    Sexo.HEMBRA,
                    caniche
            );
            maria.agregarMascota(rocky);
            maria.agregarMascota(luna);

            Cliente pedro = new Cliente(
                    "Pedro",
                    "López",
                    "28987654",
                    "11-5555-9876"
            );
            em.persist(pedro);

            Mascota michi = new Mascota(
                    "Michi",
                    LocalDate.of(2022, 11, 20),
                    Sexo.MACHO,
                    mestizoFelino
            );
            pedro.agregarMascota(michi);

            em.persist(rocky);
            em.persist(luna);
            em.persist(michi);

            // ===== Mascotas para el control de vacunaciones =====
            Cliente ana = new Cliente(
                    "Ana",
                    "Fernández",
                    "27555123",
                    "11-5555-3344"
            );
            em.persist(ana);

            Mascota rex = new Mascota(
                    "Rex",
                    LocalDate.of(2021, 5, 18),
                    Sexo.MACHO,
                    mestizo
            );
            Mascota simba = new Mascota(
                    "Simba",
                    LocalDate.of(2023, 1, 9),
                    Sexo.MACHO,
                    siames
            );
            ana.agregarMascota(rex);
            ana.agregarMascota(simba);

            Cliente diego = new Cliente(
                    "Diego",
                    "Silva",
                    "33456789",
                    "11-5555-7788"
            );
            em.persist(diego);

            Mascota bobby = new Mascota(
                    "Bobby",
                    LocalDate.of(2020, 8, 30),
                    Sexo.MACHO,
                    labrador
            );
            Mascota nala = new Mascota(
                    "Nala",
                    LocalDate.of(2022, 4, 12),
                    Sexo.HEMBRA,
                    persa
            );
            Mascota thor = new Mascota(
                    "Thor",
                    LocalDate.of(2019, 11, 3),
                    Sexo.MACHO,
                    ovejero
            );
            diego.agregarMascota(bobby);
            diego.agregarMascota(nala);
            diego.agregarMascota(thor);

            em.persist(rex);
            em.persist(simba);
            em.persist(bobby);
            em.persist(nala);
            em.persist(thor);

            em.getTransaction().commit();
            System.out.println("✅ Clientes y mascotas de ejemplo cargados correctamente.");
        } catch (Exception e) {
            deshacer(em, "Error al cargar clientes y mascotas.", e);
        }
    }

    // ==========================================================
    // TURNOS Y VACUNAS DE EJEMPLO (para visualizar el historial)
    // ==========================================================

    private static void cargarTurnosYVacunasEjemplo(EntityManager em) {
        if (hayDatos(em, "SELECT COUNT(t) FROM Turno t")) {
            return;
        }

        em.getTransaction().begin();
        try {
            TipoVacuna antirrabica = tipoVacunaPorNombre(em, "Rabisin");
            TipoVacuna quintuple = tipoVacunaPorNombre(em, "Nobivac DHPPi");

            ServicioVacunacion servAntirrabica =
                    (ServicioVacunacion) servicioPorNombre(em, "Vacuna Antirrábica");
            ServicioVacunacion servQuintuple =
                    (ServicioVacunacion) servicioPorNombre(em, "Vacuna Quíntuple");
            ServicioPeluqueria peluqueria =
                    (ServicioPeluqueria) servicioPorNombre(em, "Peluquería y Baño Completo");

            Veterinario carlos = veterinarioPorNombre(em, "Carlos", "Gómez");
            Veterinario laura = veterinarioPorNombre(em, "Laura", "Martínez");

            Mascota rocky = mascotaPorNombre(em, "Rocky");
            Mascota luna = mascotaPorNombre(em, "Luna");

            LocalDate hoy = LocalDate.now();

            // Rocky: antirrábica hace ~11 meses y medio -> la próxima cae en menos de 1 mes (alerta)
            registrarVacunacion(em, rocky, carlos, servAntirrabica,
                    hoy.minusMonths(11).minusDays(15), antirrabica,
                    "Zoetis", "1ra dosis - refuerzo anual");
            // Rocky: quíntuple hace ~3 meses -> al día
            registrarVacunacion(em, rocky, laura, servQuintuple,
                    hoy.minusMonths(3).minusDays(10), quintuple,
                    "Nobivac", "Refuerzo anual");
            // Rocky: una consulta con tratamiento para el historial
            registrarConsulta(em, rocky, carlos, hoy.minusMonths(1).minusDays(2),
                    "Control de rutina, buena salud general.",
                    "Sano, sin patologías.",
                    hoy.minusMonths(1),
                    hoy.minusMonths(1).plusDays(7),
                    "Antiparasitario oral");

            // Luna: antirrábica hace ~4 meses -> al día
            registrarVacunacion(em, luna, laura, servAntirrabica,
                    hoy.minusMonths(4).minusDays(3), antirrabica,
                    "Zoetis", "Refuerzo");
            // Luna: quíntuple hace ~13 meses -> VENCIDA (alerta)
            registrarVacunacion(em, luna, carlos, servQuintuple,
                    hoy.minusMonths(13).minusDays(15), quintuple,
                    "Nobivac", "1ra dosis");
            // Luna: una peluquería en el historial
            registrarServicioSimple(em, luna, carlos, peluqueria, hoy.minusDays(20),
                    "Baño completo y corte sanitario.");

            // ===== Vacunas de las mascotas del control de vacunaciones =====
            // Mezcla de estados: vencidas y por vencer dentro del mes de aviso.
            Mascota rex = mascotaPorNombre(em, "Rex");
            Mascota simba = mascotaPorNombre(em, "Simba");
            Mascota bobby = mascotaPorNombre(em, "Bobby");
            Mascota nala = mascotaPorNombre(em, "Nala");
            Mascota thor = mascotaPorNombre(em, "Thor");

            // Rex: antirrábica hace ~13 meses -> VENCIDA (alerta)
            registrarVacunacion(em, rex, carlos, servAntirrabica,
                    hoy.minusMonths(13).minusDays(15), antirrabica,
                    "Zoetis", "Refuerzo anual");
            // Simba: quíntuple hace ~11 meses y 20 días -> por vencer en ~10 días (alerta)
            registrarVacunacion(em, simba, laura, servQuintuple,
                    hoy.minusMonths(11).minusDays(20), quintuple,
                    "Nobivac", "1ra dosis - refuerzo anual");
            // Bobby: antirrábica hace ~11 meses y 10 días -> por vencer en ~20 días (alerta)
            registrarVacunacion(em, bobby, laura, servAntirrabica,
                    hoy.minusMonths(11).minusDays(10), antirrabica,
                    "Zoetis", "Refuerzo");
            // Nala: quíntuple hace ~14 meses -> VENCIDA (alerta)
            registrarVacunacion(em, nala, carlos, servQuintuple,
                    hoy.minusMonths(14).minusDays(5), quintuple,
                    "Nobivac", "Refuerzo anual");
            // Thor: antirrábica hace ~11 meses y 25 días -> por vencer en ~5 días (alerta)
            registrarVacunacion(em, thor, carlos, servAntirrabica,
                    hoy.minusMonths(11).minusDays(25), antirrabica,
                    "Zoetis", "1ra dosis");

            em.getTransaction().commit();
            System.out.println("✅ Turnos y vacunas de ejemplo cargados correctamente.");
        } catch (Exception e) {
            deshacer(em, "Error al cargar turnos y vacunas de ejemplo.", e);
        }
    }

    private static void registrarVacunacion(EntityManager em,
                                            Mascota mascota,
                                            Veterinario veterinario,
                                            ServicioVacunacion servicio,
                                            LocalDate fechaAplicacion,
                                            TipoVacuna tipoVacuna,
                                            String laboratorio,
                                            String observacionesDosis) {

        Turno turno = new Turno(
                fechaAplicacion.atTime(10, 0),
                EstadoTurno.ATENDIDO,
                veterinario,
                mascota
        );

        ItemTurno item = new ItemTurno(servicio, turno);
        DetalleVacunacion detalle = new DetalleVacunacion(
                tipoVacuna,
                laboratorio,
                observacionesDosis
        );
        detalle.setObservaciones(
                "Vacunación " + tipoVacuna.getNombreComercial()
        );
        item.setDetalleAtencion(detalle);
        turno.agregarItem(item);

        em.persist(turno);
    }

    private static void registrarConsulta(EntityManager em,
                                          Mascota mascota,
                                          Veterinario veterinario,
                                          LocalDate fecha,
                                          String observaciones,
                                          String diagnostico,
                                          LocalDate inicioTratamiento,
                                          LocalDate finTratamiento,
                                          String descripcionTratamiento) {

        Turno turno = new Turno(
                fecha.atTime(9, 30),
                EstadoTurno.ATENDIDO,
                veterinario,
                mascota
        );

        ItemTurno item = new ItemTurno(
                servicioPorNombre(em, "Consulta Clínica General"),
                turno
        );
        DetalleConsulta detalle = new DetalleConsulta(observaciones, diagnostico);
        detalle.agregarTratamiento(new Tratamiento(
                inicioTratamiento,
                finTratamiento,
                descripcionTratamiento
        ));
        item.setDetalleAtencion(detalle);
        turno.agregarItem(item);

        em.persist(turno);
    }

    private static void registrarServicioSimple(EntityManager em,
                                                Mascota mascota,
                                                Veterinario veterinario,
                                                Servicio servicio,
                                                LocalDate fecha,
                                                String observaciones) {

        Turno turno = new Turno(
                fecha.atTime(11, 0),
                EstadoTurno.ATENDIDO,
                veterinario,
                mascota
        );

        ItemTurno item = new ItemTurno(servicio, turno);
        DetalleAtencion detalle = new DetalleAtencion(observaciones);
        item.setDetalleAtencion(detalle);
        turno.agregarItem(item);

        em.persist(turno);
    }

    // ==========================================================
    // UTILIDADES
    // ==========================================================

    private static boolean hayDatos(EntityManager em, String jpql) {
        Long cantidad = em.createQuery(jpql, Long.class).getSingleResult();
        return cantidad != null && cantidad > 0;
    }

    private static Raza razaPorNombre(EntityManager em, String nombre) {
        return em.createQuery(
                        "SELECT r FROM Raza r WHERE r.nombre = :nombre",
                        Raza.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
    }

    private static TipoVacuna tipoVacunaPorNombre(EntityManager em, String nombre) {
        return em.createQuery(
                        "SELECT tv FROM TipoVacuna tv WHERE tv.nombreComercial = :nombre",
                        TipoVacuna.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
    }

    private static Servicio servicioPorNombre(EntityManager em, String nombre) {
        return em.createQuery(
                        "SELECT s FROM Servicio s WHERE s.nombre = :nombre",
                        Servicio.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
    }

    private static Veterinario veterinarioPorNombre(EntityManager em,
                                                    String nombre,
                                                    String apellido) {
        return em.createQuery(
                        "SELECT v FROM Veterinario v WHERE v.nombre = :nombre AND v.apellido = :apellido",
                        Veterinario.class)
                .setParameter("nombre", nombre)
                .setParameter("apellido", apellido)
                .getSingleResult();
    }

    private static Mascota mascotaPorNombre(EntityManager em, String nombre) {
        return em.createQuery(
                        "SELECT m FROM Mascota m WHERE m.nombre = :nombre",
                        Mascota.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
    }

    private static void deshacer(EntityManager em, String mensaje, Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        System.err.println("❌ " + mensaje + ": " + e.getMessage());
        e.printStackTrace();
    }
}
