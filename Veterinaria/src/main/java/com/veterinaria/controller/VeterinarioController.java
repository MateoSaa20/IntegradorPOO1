package com.veterinaria.controller;

import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Veterinario;
import com.veterinaria.repository.VeterinarioRepository;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class VeterinarioController {

    private final EntityManager em;
    private final VeterinarioRepository veterinarioRepository;

    public VeterinarioController(EntityManager em) {
        this.em = em;
        this.veterinarioRepository = new VeterinarioRepository(em);
    }

    public void registrarVeterinario(Veterinario veterinario) {
        if (veterinario.getNombre() == null || veterinario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del veterinario es obligatorio.");
        }
        if (veterinario.getMatricula() == null || veterinario.getMatricula().isBlank()) {
            throw new IllegalArgumentException("La matrícula profesional es obligatoria.");
        }

        try {
            em.getTransaction().begin();
            veterinarioRepository.guardar(veterinario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void agregarEspecialidad(long idVeterinario, Especialidad especialidad) {
        try {
            em.getTransaction().begin();

            Veterinario vet = veterinarioRepository.buscarPorId(idVeterinario)
                    .orElseThrow(() -> new IllegalArgumentException("Veterinario no encontrado con ID: " + idVeterinario));

            // Si la especialidad es nueva, la persistimos
            if (especialidad.getIdEspecialidad() == 0) {
                em.persist(especialidad);
            }

            vet.getEspecialidades().add(especialidad);
            veterinarioRepository.actualizar(vet);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public Optional<Veterinario> buscarPorMatricula(String matricula) {
        return veterinarioRepository.buscarPorMatricula(matricula);
    }

    public List<Veterinario> listarVeterinarios() {
        return veterinarioRepository.buscarTodos();
    }
}