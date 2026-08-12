package com.veterinaria.model;


import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Entity

public class ServicioGuarderia extends Servicio {

    public static final int CAPACIDAD_DEFECTO = 10;

    @jakarta.persistence.Column(name = "capacidad_maxima", nullable = false)
    private int capacidadMaxima = CAPACIDAD_DEFECTO;

    public ServicioGuarderia() {
    }

    public ServicioGuarderia(String nombre,
                             double precioBase,
                             int duracionMinutos) {

        this(nombre, precioBase, duracionMinutos, CAPACIDAD_DEFECTO);
    }

    public ServicioGuarderia(String nombre,
                             double precioBase,
                             int duracionMinutos,
                             int capacidadMaxima) {

        super(nombre, precioBase, duracionMinutos);
        this.capacidadMaxima = capacidadMaxima;
    }

    @Override
    public void validar() {
        super.validar();
        if (capacidadMaxima < 1) {
            throw new IllegalArgumentException(
                    "La capacidad máxima de la guardería debe ser al menos 1.");
        }
    }

    public int getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(int capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }

    /**
     * Regla de negocio (exclusiva de la guardería): el precio se calcula
     * multiplicando el precio por día por la cantidad de días (ingreso y
     * salida el mismo día = 1 día; salida al día siguiente = 2, etc.).
     * Mínimo 1 día. Si falta el rango, devuelve el precio base.
     */
    public double calcularSubtotalPorDias(LocalDateTime inicio, LocalDateTime fin) {

        if (inicio == null || fin == null) {
            return getPrecio();
        }

        long dias = ChronoUnit.DAYS.between(
                        inicio.toLocalDate(),
                        fin.toLocalDate())
                + 1;

        if (dias < 1) {
            dias = 1;
        }

        return getPrecio() * dias;
    }

}