package com.veterinaria.controller;

import com.veterinaria.model.DetalleAtencion;
import com.veterinaria.model.ItemTurno;
import com.veterinaria.model.Turno;
import com.veterinaria.model.EstadoTurno;
import com.veterinaria.repository.TurnoRepository;
import jakarta.persistence.EntityManager;

public class AtencionController {

    private final EntityManager em;
    private final TurnoRepository turnoRepository;

    public AtencionController(EntityManager em) {
        this.em = em;
        this.turnoRepository = new TurnoRepository(em);
    }

    public void registrarDetalleAtencion(long idTurno, long idItemTurno, DetalleAtencion detalle) {
        try {
            em.getTransaction().begin();

            Turno turno = turnoRepository.buscarPorId(idTurno)
                    .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado."));

            ItemTurno item = turno.getItems().stream()
                    .filter(i -> i.getIdItemTurno() == idItemTurno)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Item no encontrado en el turno."));

            // Vinculamos el detalle y lo persistimos
            item.setDetalleAtencion(detalle);
            em.persist(detalle);

            // Cambiamos el estado del turno a ATENDIDO
            turno.setEstado(EstadoTurno.ATENDIDO);
            turnoRepository.actualizar(turno);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
}