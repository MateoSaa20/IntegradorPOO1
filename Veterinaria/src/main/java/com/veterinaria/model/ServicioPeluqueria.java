package com.veterinaria.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "servicios_peluqueria")
public class ServicioPeluqueria extends Servicio {

    public ServicioPeluqueria() {
    }

    public ServicioPeluqueria(String nombre,
                              double precioBase,
                              int duracionMinutos) {

        super(nombre, precioBase, duracionMinutos);
    }
}