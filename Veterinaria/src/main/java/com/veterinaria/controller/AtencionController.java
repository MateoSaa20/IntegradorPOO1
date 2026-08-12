package com.veterinaria.controller;

import com.veterinaria.model.DetalleAtencion;
import com.veterinaria.model.DetalleConsulta;
import com.veterinaria.model.DetalleVacunacion;
import com.veterinaria.model.EstadoTurno;
import com.veterinaria.model.ItemTurno;
import com.veterinaria.model.Servicio;
import com.veterinaria.model.ServicioConsulta;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.Turno;
import com.veterinaria.repository.TurnoRepository;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Map;

/**
 * Orquesta la atención de los turnos confirmados aplicando las reglas de
 * negocio:
 * - Solo se listan y atienden turnos CONFIRMADO.
 * - Los detalles son opcionales por servicio, pero un detalle cargado no
 *   puede quedar vacío (cada tipo valida su contenido mínimo).
 * - El tipo de detalle debe corresponderse con el servicio del item.
 * - Al atender, el turno pasa a ATENDIDO (solo desde CONFIRMADO).
 */
public class AtencionController {

    private final EntityManager em;
    private final TurnoRepository turnoRepository;

    public AtencionController(EntityManager em) {
        this.em = em;
        this.turnoRepository = new TurnoRepository(em);
    }

    /**
     * Solo los turnos confirmados, que son los que el veterinario puede
     * atender.
     */
    public List<Turno> listarTurnosConfirmados() {
        return turnoRepository.findByEstado(EstadoTurno.CONFIRMADO);
    }

    public Turno buscarTurno(Long idTurno) {
        return turnoRepository.buscarPorId(idTurno).orElse(null);
    }

    /**
     * Persiste únicamente los detalles cargados en el mapa (itemId -> detalle),
     * sin cambiar el estado del turno.
     */
    public void guardarDetalles(Long idTurno,
                                Map<Long, DetalleAtencion> detallesPorItem) throws Exception {

        Turno turno = turnoRepository.buscarPorId(idTurno)
                .orElseThrow(() -> new Exception("No se encontró el turno con ID: " + idTurno));

        try {
            em.getTransaction().begin();
            aplicarDetalles(turno, detallesPorItem);
            turnoRepository.actualizar(turno);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Persiste los detalles cargados y pasa el turno a ATENDIDO. La
     * transición valida que el turno esté CONFIRMADO.
     */
    public void atenderTurno(Long idTurno,
                             Map<Long, DetalleAtencion> detallesPorItem) throws Exception {

        Turno turno = turnoRepository.buscarPorId(idTurno)
                .orElseThrow(() -> new Exception("No se encontró el turno con ID: " + idTurno));

        try {
            em.getTransaction().begin();
            aplicarDetalles(turno, detallesPorItem);
            turno.atender();
            turnoRepository.actualizar(turno);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    private void aplicarDetalles(Turno turno,
                                 Map<Long, DetalleAtencion> detallesPorItem) throws Exception {

        if (detallesPorItem == null) {
            return;
        }

        for (Map.Entry<Long, DetalleAtencion> entry : detallesPorItem.entrySet()) {

            ItemTurno item = turno.getItems().stream()
                    .filter(i -> i.getIdItem().equals(entry.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new Exception(
                            "El turno no contiene el servicio solicitado."
                    ));

            DetalleAtencion detalle = entry.getValue();

            validarCompatibilidad(item, detalle);
            detalle.validar();

            // El tipo de vacuna siempre se toma del servicio (no se edita).
            if (detalle instanceof DetalleVacunacion vacunacion
                    && item.getServicio() instanceof ServicioVacunacion servicio) {
                vacunacion.setTipoVacuna(servicio.getTipoVacuna());
            }

            item.setDetalleAtencion(detalle);
        }
    }

    private void validarCompatibilidad(ItemTurno item,
                                       DetalleAtencion detalle) throws Exception {

        Servicio servicio = item.getServicio();

        if (servicio instanceof ServicioConsulta && !(detalle instanceof DetalleConsulta)) {
            throw new Exception("El servicio de consulta debe registrarse con un detalle de consulta.");
        }

        if (servicio instanceof ServicioVacunacion && !(detalle instanceof DetalleVacunacion)) {
            throw new Exception("El servicio de vacunación debe registrarse con un detalle de vacunación.");
        }

        boolean esConsultaOVacunacion =
                servicio instanceof ServicioConsulta || servicio instanceof ServicioVacunacion;

        if (!esConsultaOVacunacion
                && (detalle instanceof DetalleConsulta || detalle instanceof DetalleVacunacion)) {
            throw new Exception("El detalle no corresponde al servicio seleccionado.");
        }
    }
}
