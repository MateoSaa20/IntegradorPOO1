package com.veterinaria.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "tratamientos")
public class Tratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tratamiento")
    private Long idTratamiento;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false, length = 500)
    private String descripcion;

   

    public Tratamiento() {
    }

    public Tratamiento(LocalDate fechaInicio,
                       LocalDate fechaFin,
                       String descripcion) {

        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.descripcion = descripcion;
    }

    // ===== GETTERS =====

    public Long getIdTratamiento() {
        return idTratamiento;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // ===== SETTERS =====

    public void setIdTratamiento(Long idTratamiento) {
        this.idTratamiento = idTratamiento;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Regla de negocio: un tratamiento exige descripción y un rango de
     * fechas válido (fin no anterior a inicio).
     */
    public void validar() {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción del tratamiento es obligatoria.");
        }
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("El tratamiento debe tener fecha de inicio y de fin.");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
