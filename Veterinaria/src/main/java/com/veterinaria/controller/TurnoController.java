package com.veterinaria.controller;

import com.veterinaria.model.Turno;
import com.veterinaria.repository.TurnoRepository;
import jakarta.persistence.EntityManager;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TurnoController {

    private final EntityManager em;
    private final TurnoRepository turnoRepository;

    public TurnoController(EntityManager em) {
        this.em = em;
        this.turnoRepository = new TurnoRepository(em);
    }

/**
     * Método principal para guardar un nuevo turno.
     */
    public void agendarTurno(Turno nuevoTurno) throws Exception {
        // 1. Extraemos los datos necesarios para la validación
        LocalDate fechaTurno = nuevoTurno.getFechaHora().toLocalDate();
        Long idVeterinario = nuevoTurno.getVeterinario().getIdVeterinario();

        // 2. Buscamos los turnos previos de ese veterinario para ese mismo día
        List<Turno> turnosDelDia = turnoRepository.findByVeterinarioAndFecha(idVeterinario, fechaTurno);

        // 3. Ejecutamos la regla de negocio (arrojará Exception si hay solapamiento)
        nuevoTurno.validarDisponibilidad(turnosDelDia);

        // 4. Si la validación es exitosa, guardamos en la base de datos
        // IMPORTANTE: Cambia "guardar" por el método que tengas definido en tu BaseRepository (ej. crear, persist, etc.)
        turnoRepository.guardar(nuevoTurno);
    }


    public void cancelarTurno(Long idTurno) throws Exception {
        Turno turno = turnoRepository.buscarPorId(idTurno)
        .orElseThrow(() -> new Exception("No se encontró el turno con ID: " + idTurno)); // O como lo busques en tu BaseRepository
        
        // Le pasamos el momento exacto en el que el usuario hizo click en "Cancelar"
        turno.cancelar(LocalDateTime.now());
        
        turnoRepository.guardar(turno); // O actualizar/merge
    }

    public List<Turno> listarTurnos() {
        return turnoRepository.buscarTodos();
    }
}