package com.veterinaria.controller;

import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Veterinario;
import com.veterinaria.repository.EspecialidadRepository;
import com.veterinaria.repository.VeterinarioRepository;
import com.veterinaria.util.TextoUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Orquesta las operaciones sobre los veterinarios aplicando las reglas de
 * negocio:
 * - El nombre, apellido y la matrícula profesional son obligatorios.
 * - La matrícula profesional es única.
 * - Un veterinario con turnos asignados no puede eliminarse.
 */
public class VeterinarioController {

    private final EntityManager em;
    private final VeterinarioRepository veterinarioRepository;
    private final EspecialidadRepository especialidadRepository;

    public VeterinarioController(EntityManager em) {
        this.em = em;
        this.veterinarioRepository = new VeterinarioRepository(em);
        this.especialidadRepository = new EspecialidadRepository(em);
    }

    public void registrarVeterinario(Veterinario veterinario) {
        veterinario.validar();

        try {
            em.getTransaction().begin();
            veterinarioRepository.guardar(veterinario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (esMatriculaDuplicada(veterinario.getMatricula())) {
                throw new IllegalArgumentException(
                        "Ya existe un veterinario registrado con la matrícula " + veterinario.getMatricula() + "."
                );
            }
            throw e;
        }
    }

    public Veterinario actualizar(Veterinario veterinario) {
        veterinario.validar();

        try {
            em.getTransaction().begin();
            Veterinario actualizado = veterinarioRepository.actualizar(veterinario);
            em.getTransaction().commit();
            return actualizado;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void eliminar(Long idVeterinario) {
        if (idVeterinario == null || veterinarioRepository.tieneTurnos(idVeterinario)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un veterinario que tiene turnos asignados."
            );
        }

        try {
            em.getTransaction().begin();
            veterinarioRepository.buscarPorId(idVeterinario).ifPresent(veterinario -> {
                veterinarioRepository.eliminar(veterinario);
            });
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public void agregarEspecialidad(long idVeterinario, Especialidad especialidad) {
        try {
            em.getTransaction().begin();

            Veterinario vet = veterinarioRepository.buscarPorId(idVeterinario)
                    .orElseThrow(() -> new IllegalArgumentException("Veterinario no encontrado con ID: " + idVeterinario));

            // Si la especialidad es nueva, la persistimos
            if (especialidad.getIdEspecialidad() == null) {
                em.persist(especialidad);
            } else {
                especialidad = em.merge(especialidad);
            }

            vet.getEspecialidades().add(especialidad);
            veterinarioRepository.actualizar(vet);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Registra una especialidad nueva (creación inline desde la pantalla de
     * veterinarios).
     */
    public Especialidad registrarEspecialidad(Especialidad especialidad) {
        if (especialidad.getNombre() == null || especialidad.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la especialidad es obligatorio.");
        }
        especialidad.setNombre(TextoUtil.capitalizar(especialidad.getNombre()));
        try {
            em.getTransaction().begin();
            especialidadRepository.guardar(especialidad);
            em.getTransaction().commit();
            return especialidad;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    public List<Especialidad> listarEspecialidades() {
        return especialidadRepository.buscarTodos();
    }

    public Optional<Veterinario> buscarPorMatricula(String matricula) {
        return veterinarioRepository.buscarPorMatricula(matricula);
    }

    public List<Veterinario> listarVeterinarios() {
        return veterinarioRepository.buscarTodos();
    }

    private boolean esMatriculaDuplicada(String matricula) {
        return buscarPorMatricula(matricula).isPresent();
    }
}
