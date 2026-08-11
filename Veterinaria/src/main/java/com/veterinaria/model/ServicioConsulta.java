package com.veterinaria.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity

public class ServicioConsulta extends Servicio {

    public ServicioConsulta() {
    }

    public ServicioConsulta(String nombre,
                            double precioBase,
                            int duracionMinutos) {

        super(nombre, precioBase, duracionMinutos);
    }

}