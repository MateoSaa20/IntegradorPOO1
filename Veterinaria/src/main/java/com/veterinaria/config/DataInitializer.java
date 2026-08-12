package com.veterinaria.config;

import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Especie;
import com.veterinaria.model.Raza;
import com.veterinaria.model.ServicioConsulta;
import com.veterinaria.model.ServicioGuarderia;
import com.veterinaria.model.ServicioPeluqueria;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.TipoVacuna;
import com.veterinaria.model.Veterinario;
import com.veterinaria.repository.EspecieRepository;
import com.veterinaria.repository.RazaRepository;
import jakarta.persistence.EntityManager;

public class DataInitializer {

    public static void cargarDatosIniciales(EntityManager em) {
        RazaRepository razaRepository = new RazaRepository(em);
        EspecieRepository especieRepository = new EspecieRepository(em);

        // -----------------------------------------------------------
        // 1. Cargar Especies y Razas
        // -----------------------------------------------------------
        try {
            if (razaRepository.buscarTodos().isEmpty()) {
                em.getTransaction().begin();

                Especie canino = new Especie("Canino");
                Especie felino = new Especie("Felino");
                especieRepository.guardar(canino);
                especieRepository.guardar(felino);

                razaRepository.guardar(new Raza("Mestizo", canino));
                razaRepository.guardar(new Raza("Labrador", canino));
                razaRepository.guardar(new Raza("Caniche", canino));
                razaRepository.guardar(new Raza("Ovejero Alemán", canino));

                razaRepository.guardar(new Raza("Mestizo Felino", felino));
                razaRepository.guardar(new Raza("Siamés", felino));
                razaRepository.guardar(new Raza("Persa", felino));

                em.getTransaction().commit();
                System.out.println("✅ Especies y Razas cargadas con éxito.");
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("❌ Error al cargar Especies/Razas: " + e.getMessage());
            e.printStackTrace();
        }

       // En DataInitializer.java:
try {
    Long countVet = em.createQuery("SELECT COUNT(v) FROM Veterinario v", Long.class).getSingleResult();

    if (countVet == 0) {
        em.getTransaction().begin();

        Especialidad espGeneral = new Especialidad("Clínica General", "Atención médica integral");
        Especialidad espCirugia = new Especialidad("Cirugía", "Cirugías generales y especializadas");
        em.persist(espGeneral);
        em.persist(espCirugia);

        Veterinario vet1 = new Veterinario();
        vet1.setMatricula("MP-1042");
        vet1.setNombre("Carlos");
        vet1.setApellido("Gómez");
        Veterinario vet2 = new Veterinario();
        vet2.setMatricula("MP-2085");
        vet2.setNombre("Laura");
        vet2.setApellido("Martínez");
        em.persist(vet1);
        em.persist(vet2);

        em.getTransaction().commit();
        System.out.println("✅ Veterinarios cargados correctamente.");
    }
} catch (Exception e) {
    if (em.getTransaction().isActive()) em.getTransaction().rollback();
    System.err.println("❌ Error al cargar veterinarios: " + e.getMessage());
    e.printStackTrace();
}
    }
    public static void cargarServiciosIniciales() {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            // Verificar si ya existen servicios registrados
            Long cantidadServicios = em.createQuery("SELECT COUNT(s) FROM Servicio s", Long.class)
                                       .getSingleResult();

            if (cantidadServicios == 0) {
                em.getTransaction().begin();
// 1. Instanciar y persistir los Tipos de Vacuna en la BD
                // (Al hacer persist, la BD le genera el ID autoincremental)
                TipoVacuna antirrabicaTv = new TipoVacuna("Rabisin", "Rabia", 12);
                TipoVacuna quintupleTv = new TipoVacuna("Nobivac DHPPi", "Moquillo, Parvovirus, Hepatitis, Leptospirosis", 12);

                em.persist(antirrabicaTv);
                em.persist(quintupleTv);

                // Forzamos la sincronización para que Hibernate obtenga los IDs generados
                em.flush();

                // 2. Instanciar los Servicios pasando el objeto TipoVacuna ya persistido
                ServicioConsulta consulta = new ServicioConsulta("Consulta Clínica General", 6500.0, 30);
                ServicioVacunacion antirrabica = new ServicioVacunacion("Vacuna Antirrábica", 9000.0, 15, antirrabicaTv);
                ServicioVacunacion quintuple = new ServicioVacunacion("Vacuna Quíntuple", 11500.0, 15, quintupleTv);
                ServicioPeluqueria peluqueria = new ServicioPeluqueria("Peluquería y Baño Completo", 14000.0, 60);
                ServicioGuarderia guarderia = new ServicioGuarderia("Guardería Canina/Felina (por Día)", 18000.0, 1440);

                // 3. Persistir los servicios
                em.persist(consulta);
                em.persist(antirrabica);
                em.persist(quintuple);
                em.persist(peluqueria);
                em.persist(guarderia);

                em.getTransaction().commit();
                System.out.println("✅ SERVICIOS INICIALES CARGADOS EXITOSAMENTE EN LA BD.");
            } else {
                System.out.println("ℹ️ Los servicios ya se encontraban cargados en la BD.");
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("❌ ERROR AL CARGAR SERVICIOS INICIALES: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}