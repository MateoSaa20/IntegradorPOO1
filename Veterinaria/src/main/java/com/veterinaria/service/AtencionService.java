package com.veterinaria.service;

import com.veterinaria.model.DetalleAtencion;
import com.veterinaria.model.DetalleConsulta;
import com.veterinaria.model.DetalleVacunacion;
import com.veterinaria.model.EstadoTurno;
import com.veterinaria.model.ItemTurno;
import com.veterinaria.model.Servicio;
import com.veterinaria.model.ServicioConsulta;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.Turno;
import com.veterinaria.model.Tratamiento;
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
public class AtencionService {

    private final Transaccion transaccion;
    private final TurnoRepository turnoRepository;

    public AtencionService(EntityManager em) {
        this.transaccion = new Transaccion(em);
        this.turnoRepository = new TurnoRepository(em);
    }

    /**
     * Solo los turnos confirmados, que son los que el veterinario puede
     * atender.
     */
    public List<Turno> listarTurnosConfirmados() {
        return turnoRepository.findByEstado(EstadoTurno.CONFIRMADO);
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
            transaccion.ejecutar(() -> {
                aplicarDetalles(turno, detallesPorItem);
                turnoRepository.actualizar(turno);
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
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
            transaccion.ejecutar(() -> {
                aplicarDetalles(turno, detallesPorItem);
                turno.atender();
                turnoRepository.actualizar(turno);
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
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

            // Si el item ya tiene un detalle persistido se reutiliza el mismo
            // registro (se copian los campos) en lugar de reemplazarlo por uno
            // nuevo. Reemplazarlo obligaría a borrar la fila anterior e insertar
            // otra con el mismo id_item_turno (clave única), lo que provoca una
            // violación de unicidad por el orden del flush.
            DetalleAtencion actual = item.getDetalleAtencion();

            if (actual == null) {
                item.setDetalleAtencion(detalle);
            } else {
                copiarDetalle(actual, detalle);
            }
        }
    }

    private void copiarDetalle(DetalleAtencion destino,
                               DetalleAtencion origen) {

        if (destino == origen) {
            return;
        }

        if (destino instanceof DetalleConsulta destinoConsulta
                && origen instanceof DetalleConsulta origenConsulta) {

            destinoConsulta.setObservaciones(origenConsulta.getObservaciones());
            destinoConsulta.setDiagnostico(origenConsulta.getDiagnostico());
            destinoConsulta.setTratamientos(copiarTratamientos(
                    origenConsulta.getTratamientos()
            ));
            return;
        }

        if (destino instanceof DetalleVacunacion destinoVacuna
                && origen instanceof DetalleVacunacion origenVacuna) {

            destinoVacuna.setObservaciones(origenVacuna.getObservaciones());
            destinoVacuna.setTipoVacuna(origenVacuna.getTipoVacuna());
            destinoVacuna.setLaboratorioOMarca(origenVacuna.getLaboratorioOMarca());
            destinoVacuna.setObservacionesDosis(origenVacuna.getObservacionesDosis());
            return;
        }

        destino.setObservaciones(origen.getObservaciones());
    }

    private List<Tratamiento> copiarTratamientos(List<Tratamiento> tratamientos) {

        return tratamientos.stream()
                .map(t -> new Tratamiento(
                        t.getFechaInicio(),
                        t.getFechaFin(),
                        t.getDescripcion()
                ))
                .toList();
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
