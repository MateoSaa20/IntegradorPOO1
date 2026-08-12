package com.veterinaria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "item_guarderia")
public class ItemGuarderia extends ItemTurno {

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    public ItemGuarderia() {}

    public ItemGuarderia(ServicioGuarderia servicio, Turno turno, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        super(servicio, turno);
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;

        recalcularValoresHistoricos(servicio);
    }

    /**
     * Regla de negocio: el precio cobrado depende de la cantidad de días
     * (ingreso y salida el mismo día = 1 día; salida al día siguiente = 2, etc.).
     * El tiempo cobrado es la cantidad de minutos entre ingreso y salida.
     */
    private void recalcularValoresHistoricos(ServicioGuarderia servicio) {
        if (fechaHoraInicio != null && fechaHoraFin != null && servicio != null) {
            long minutosTotales = ChronoUnit.MINUTES.between(fechaHoraInicio, fechaHoraFin);
            if (minutosTotales < 0) minutosTotales = 0;

            setTiempoAlMomento((int) minutosTotales);
            setPrecioAlMomento(servicio.calcularSubtotalPorDias(fechaHoraInicio, fechaHoraFin));
        }
    }

    /**
     * Regla de negocio: la guardería no puede iniciar en un día u hora anterior
     * a la actual, y la salida debe ser posterior al ingreso.
     */
    public void validarRango(LocalDateTime ahora) throws Exception {
        if (fechaHoraInicio == null || fechaHoraFin == null) {
            throw new Exception("Debe indicar el ingreso y la salida de la guardería.");
        }

        if (fechaHoraInicio.isBefore(ahora)) {
            throw new Exception("La guardería no puede iniciar en un día u hora anterior a la fecha y hora actual.");
        }

        if (!fechaHoraFin.isAfter(fechaHoraInicio)) {
            throw new Exception("La salida de la guardería debe ser posterior al ingreso.");
        }
    }

    /**
     * Cantidad de días cobrados según la regla: días entre ingreso y salida + 1 (mínimo 1).
     */
    public long calcularCantidadDias() {
        if (fechaHoraInicio == null || fechaHoraFin == null) {
            return 1;
        }
        long dias = ChronoUnit.DAYS.between(fechaHoraInicio.toLocalDate(), fechaHoraFin.toLocalDate()) + 1;
        return Math.max(dias, 1);
    }

    // Getters y Setters
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
}
