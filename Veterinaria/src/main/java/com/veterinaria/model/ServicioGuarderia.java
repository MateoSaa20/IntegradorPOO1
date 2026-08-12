package com.veterinaria.model;


import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Entity

public class ServicioGuarderia extends Servicio {


    public ServicioGuarderia() {
    }

    public ServicioGuarderia(String nombre,
                             double precioBase,
                             int duracionMinutos) {

        super(nombre, precioBase, duracionMinutos);                                      
    }

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