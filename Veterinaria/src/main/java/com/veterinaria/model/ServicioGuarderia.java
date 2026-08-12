package com.veterinaria.model;


import jakarta.persistence.Entity;


@Entity

public class ServicioGuarderia extends Servicio {


    public ServicioGuarderia() {
    }

    public ServicioGuarderia(String nombre,
                             double precioBase,
                             int duracionMinutos) {

        super(nombre, precioBase, duracionMinutos);                                      
    }

}