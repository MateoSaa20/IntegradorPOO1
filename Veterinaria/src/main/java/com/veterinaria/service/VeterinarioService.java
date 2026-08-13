package com.veterinaria.service;

import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Veterinario;
import com.veterinaria.repository.EspecialidadRepository;
import com.veterinaria.repository.VeterinarioRepository;
import com.veterinaria.util.TextoUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Coordina la persistencia y las reglas de negocio de los veterinarios.
 * Los controladores no tocan el EntityManager directamente.
 */
public class VeterinarioService {

    private final Transaccion transaccion;
    private final VeterinarioRepository veterinarioRepository;
    private final EspecialidadRepository especialidadRepository;

    public VeterinarioService(EntityManager em) {
        this.transaccion = new Transaccion(em);
        this.veterinarioRepository = new VeterinarioRepository(em);
        this.especialidadRepository = new EspecialidadRepository(em);
    }

    public void registrarVeterinario(Veterinario veterinario) {
        veterinario.validar();

        try {
            transaccion.ejecutar(() -> veterinarioRepository.guardar(veterinario));
        } catch (Exception e) {
            if (esMatriculaDuplicada(veterinario.getMatricula())) {
                throw new IllegalArgumentException(
                        "Ya existe un veterinario registrado con la matrícula "
                                + veterinario.getMatricula() + "."
                );
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al registrar el veterinario: " + e.getMessage(), e);
        }
    }

    public Veterinario actualizarVeterinario(Veterinario veterinario) {
        veterinario.validar();

        try {
            return transaccion.ejecutarConResultado(
                    () -> veterinarioRepository.actualizar(veterinario));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al actualizar el veterinario: " + e.getMessage(), e);
        }
    }

    public void eliminarVeterinario(Long idVeterinario) {
        if (idVeterinario == null || veterinarioRepository.tieneTurnos(idVeterinario)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un veterinario que tiene turnos asignados."
            );
        }

        try {
            transaccion.ejecutar(() ->
                    veterinarioRepository.buscarPorId(idVeterinario)
                            .ifPresent(veterinarioRepository::eliminar));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al eliminar el veterinario: " + e.getMessage(), e);
        }
    }

    public void agregarEspecialidad(long idVeterinario, Especialidad especialidad) {
        try {
            transaccion.ejecutar(() -> {
                Veterinario vet = veterinarioRepository.buscarPorId(idVeterinario)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Veterinario no encontrado con ID: " + idVeterinario));

                if (especialidad.getIdEspecialidad() == null) {
                    especialidadRepository.guardar(especialidad);
                } else {
                    especialidadRepository.actualizar(especialidad);
                }

                vet.getEspecialidades().add(especialidad);
                veterinarioRepository.actualizar(vet);
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al agregar la especialidad: " + e.getMessage(), e);
        }
    }

    public Especialidad registrarEspecialidad(Especialidad especialidad) {
        if (especialidad.getNombre() == null || especialidad.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la especialidad es obligatorio.");
        }
        especialidad.setNombre(TextoUtil.capitalizar(especialidad.getNombre()));

        try {
            return transaccion.ejecutarConResultado(() -> {
                especialidadRepository.guardar(especialidad);
                return especialidad;
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al registrar la especialidad: " + e.getMessage(), e);
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
