package com.veterinaria.controller;

import com.veterinaria.model.EstadoTurno;
import com.veterinaria.model.Turno;
import com.veterinaria.repository.TurnoRepository;
import jakarta.persistence.EntityManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TurnoController {

    private final EntityManager em;
    private final TurnoRepository turnoRepository;

    public TurnoController(EntityManager em) {
        this.em = em;
        this.turnoRepository = new TurnoRepository(em);
    }

    public void agendarTurno(Turno turno) {
        if (turno.getFechaHora() == null || turno.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha del turno debe ser futura.");
        }
        if (turno.getMascota() == null) {
            throw new IllegalArgumentException("El turno debe tener una mascota asignada.");
        }
        if (turno.getVeterinario() == null) {
            throw new IllegalArgumentException("El turno debe tener un veterinario asignado.");
        }

        try {
            em.getTransaction().begin();
            turno.setEstado(EstadoTurno.PENDIENTE);
            turnoRepository.guardar(turno);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void cancelarTurno(long idTurno) {
        try {
            em.getTransaction().begin();

            Turno turno = turnoRepository.buscarPorId(idTurno)
                    .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado con ID: " + idTurno));

            // Regla de Negocio: Cancelar únicamente con más de 24 horas de anticipación
            LocalDateTime ahora = LocalDateTime.now();
            long horasDiferencia = Duration.between(ahora, turno.getFechaHora()).toHours();

            if (horasDiferencia < 24) {
                throw new IllegalStateException("No se puede cancelar un turno con menos de 24 horas de anticipación.");
            }

            turno.setEstado(EstadoTurno.CANCELADO);
            turnoRepository.actualizar(turno);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public List<Turno> listarTurnos() {
        return turnoRepository.buscarTodos();
    }
}