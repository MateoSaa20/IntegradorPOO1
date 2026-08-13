package com.veterinaria.controller;

import com.veterinaria.model.DetalleAtencion;
import com.veterinaria.model.Turno;
import com.veterinaria.service.AtencionService;

import java.util.List;
import java.util.Map;

/**
 * Orquesta la atención de los turnos confirmados aplicando las reglas de
 * negocio delegadas a la capa de servicios:
 * - Solo se listan y atienden turnos CONFIRMADO.
 * - Los detalles son opcionales por servicio, pero un detalle cargado no
 *   puede quedar vacío (cada tipo valida su contenido mínimo).
 * - El tipo de detalle debe corresponderse con el servicio del item.
 * - Al atender, el turno pasa a ATENDIDO (solo desde CONFIRMADO).
 */
public class AtencionController {

    private final AtencionService atencionService;

    public AtencionController(AtencionService atencionService) {
        this.atencionService = atencionService;
    }

    /**
     * Solo los turnos confirmados, que son los que el veterinario puede
     * atender.
     */
    public List<Turno> listarTurnosConfirmados() {
        return atencionService.listarTurnosConfirmados();
    }

    /**
     * Persiste únicamente los detalles cargados en el mapa (itemId -> detalle),
     * sin cambiar el estado del turno.
     */
    public void guardarDetalles(Long idTurno,
                                Map<Long, DetalleAtencion> detallesPorItem) throws Exception {
        atencionService.guardarDetalles(idTurno, detallesPorItem);
    }

    /**
     * Persiste los detalles cargados y pasa el turno a ATENDIDO. La
     * transición valida que el turno esté CONFIRMADO.
     */
    public void atenderTurno(Long idTurno,
                             Map<Long, DetalleAtencion> detallesPorItem) throws Exception {
        atencionService.atenderTurno(idTurno, detallesPorItem);
    }
}
