package com.veterinaria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@DiscriminatorValue("GUARDERIA")
public class ItemGuarderia extends ItemTurno {

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    public ItemGuarderia() {}

    public ItemGuarderia(Servicio servicio, Turno turno, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        super(servicio, turno);
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        
        // Regla de Negocio: Calcular el precio y la duración cobrada según el rango de tiempo
        recalcularValoresHistoricos(servicio);
    }

    /**
     * Calcula la cantidad de horas o días entre fechaHoraInicio y fechaHoraFin
     * para asignar el precioCobrado y duracionCobrada dinámicamente.
     */
    private void recalcularValoresHistoricos(Servicio servicio) {
        if (fechaHoraInicio != null && fechaHoraFin != null && servicio != null) {
            long minutosTotales = Duration.between(fechaHoraInicio, fechaHoraFin).toMinutes();
            
            // Si el servicio se cobra por día (ej: cada 24hs)
            long dias = (long) Math.ceil((double) minutosTotales / (24 * 60));
            if (dias < 1) dias = 1; // Cobro mínimo de 1 día

            setTiempoAlMomento((int) minutosTotales);
            setPrecioAlMomento(servicio.getPrecio() * dias);
        }
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