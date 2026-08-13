package com.veterinaria.model;

import jakarta.persistence.Entity;

@Entity

public class ServicioPeluqueria extends Servicio {

    public ServicioPeluqueria() {
    }

    public ServicioPeluqueria(String nombre,
                              double precioBase,
                              int duracionMinutos) {

        super(nombre, precioBase, duracionMinutos);
    }
}