package com.veterinaria.controller;

import com.veterinaria.model.Especialidad;
import com.veterinaria.model.Veterinario;
import com.veterinaria.service.VeterinarioService;

import java.util.List;
import java.util.Optional;

/**
 * Orquesta las operaciones sobre los veterinarios aplicando las reglas de
 * negocio delegadas a la capa de servicios:
 * - El nombre, apellido y la matrícula profesional son obligatorios.
 * - La matrícula profesional es única.
 * - Un veterinario con turnos asignados no puede eliminarse.
 */
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    public VeterinarioController(VeterinarioService veterinarioService) {
        this.veterinarioService = veterinarioService;
    }

    public void registrarVeterinario(Veterinario veterinario) {
        veterinarioService.registrarVeterinario(veterinario);
    }

    public Veterinario actualizar(Veterinario veterinario) {
        return veterinarioService.actualizarVeterinario(veterinario);
    }

    public void eliminar(Long idVeterinario) {
        veterinarioService.eliminarVeterinario(idVeterinario);
    }

    public void agregarEspecialidad(long idVeterinario, Especialidad especialidad) {
        veterinarioService.agregarEspecialidad(idVeterinario, especialidad);
    }

    /**
     * Registra una especialidad nueva (creación inline desde la pantalla de
     * veterinarios).
     */
    public Especialidad registrarEspecialidad(Especialidad especialidad) {
        return veterinarioService.registrarEspecialidad(especialidad);
    }

    public List<Especialidad> listarEspecialidades() {
        return veterinarioService.listarEspecialidades();
    }

    public Optional<Veterinario> buscarPorMatricula(String matricula) {
        return veterinarioService.buscarPorMatricula(matricula);
    }

    public List<Veterinario> listarVeterinarios() {
        return veterinarioService.listarVeterinarios();
    }
}
