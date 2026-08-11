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
    public void agendarTurno(Turno turno) {
    try {
        em.getTransaction().begin();
        
        // Si la mascota o el veterinario vienen desasociados de la sesión, los reasociamos con merge
        if (turno.getMascota() != null) {
            turno.setMascota(em.merge(turno.getMascota()));
        }
        if (turno.getVeterinario() != null) {
            turno.setVeterinario(em.merge(turno.getVeterinario()));
        }

        em.persist(turno);
        
        em.getTransaction().commit();
        System.out.println("✅ TURNO GUARDADO EXITOSAMENTE CON ID: " + turno.getIdTurno());
    } catch (Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        System.err.println("❌ ERROR AL GUARDAR TURNO EN BD: " + e.getMessage());
        e.printStackTrace();
        throw e; // Relanzamos para que la alerta de JavaFX lo muestre
    }
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