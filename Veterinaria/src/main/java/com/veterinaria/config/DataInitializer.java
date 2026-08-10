package com.veterinaria.config;

import com.veterinaria.model.Especie;
import com.veterinaria.model.Raza;
import com.veterinaria.repository.EspecieRepository;
import com.veterinaria.repository.RazaRepository;
import jakarta.persistence.EntityManager;

public class DataInitializer {

    public static void cargarDatosIniciales(EntityManager em) {
        RazaRepository razaRepository = new RazaRepository(em);
        EspecieRepository especieRepository = new EspecieRepository(em);

        // Si no existen razas en la BD, creamos especies y razas básicas
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
        }
    }
}