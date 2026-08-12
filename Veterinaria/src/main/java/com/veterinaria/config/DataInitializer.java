package com.veterinaria.config;

import com.veterinaria.model.Cliente;
import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Especie;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.Raza;
import com.veterinaria.model.ServicioConsulta;
import com.veterinaria.model.ServicioGuarderia;
import com.veterinaria.model.ServicioPeluqueria;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.Sexo;
import com.veterinaria.model.TipoVacuna;
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
                    12
            );
            TipoVacuna quintuple = new TipoVacuna(
                    "Nobivac DHPPi",
                    "Moquillo, Parvovirus, Hepatitis, Leptospirosis",
                    12
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

            Cliente maria = new Cliente(
                    "María",
                    "González",
                    "30.123.456",
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
                    "28.987.654",
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

            em.getTransaction().commit();
            System.out.println("✅ Clientes y mascotas de ejemplo cargados correctamente.");
        } catch (Exception e) {
            deshacer(em, "Error al cargar clientes y mascotas.", e);
        }
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

    private static void deshacer(EntityManager em, String mensaje, Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        System.err.println("❌ " + mensaje + ": " + e.getMessage());
        e.printStackTrace();
    }
}
