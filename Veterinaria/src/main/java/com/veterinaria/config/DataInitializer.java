package com.veterinaria.config;

import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Especie;
import com.veterinaria.model.Raza;
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
}