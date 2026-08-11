package com.veterinaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipos_vacuna")
public class TipoVacuna{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreComercial;

    @Column(nullable = false)
    private String enfermedadQuePreviene;

    @Column(nullable = false)
    private int periodicidadMeses;

    public TipoVacuna() {
    }

    public TipoVacuna(String nombreComercial,
                      String enfermedadQuePreviene,
                      int periodicidadMeses) {

        this.nombreComercial = nombreComercial;
        this.enfermedadQuePreviene = enfermedadQuePreviene;
        this.periodicidadMeses = periodicidadMeses;
    }

    public Long getId() {
        return id;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getEnfermedadQuePreviene() {
        return enfermedadQuePreviene;
    }

    public void setEnfermedadQuePreviene(String enfermedadQuePreviene) {
        this.enfermedadQuePreviene = enfermedadQuePreviene;
    }

    public int getPeriodicidadMeses() {
        return periodicidadMeses;
    }

    public void setPeriodicidadMeses(int periodicidadMeses) {
        this.periodicidadMeses = periodicidadMeses;
    }
}