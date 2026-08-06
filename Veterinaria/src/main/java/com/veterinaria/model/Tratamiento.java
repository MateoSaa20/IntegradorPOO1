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

    // Getters y Setters
}
