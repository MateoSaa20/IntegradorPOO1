package com.veterinaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "servicios")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private double precioBase;

    @Column(nullable = false)
    private int duracionMinutos;

    public Servicio() {
    }

    public Servicio(String nombre,
                    double precioBase,
                    int duracionMinutos) {

        this.nombre = nombre;
        this.precioBase = precioBase;
        this.duracionMinutos = duracionMinutos;
    }

    public Long getIdServicio() {
        return idServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecioBase(double precioBase) {
        this.precioBase = precioBase;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    @Override
    public String toString() {
        return nombre;
    }
}