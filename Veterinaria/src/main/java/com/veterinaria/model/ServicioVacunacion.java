package com.veterinaria.model;

import jakarta.persistence.*;

@Entity

public class ServicioVacunacion extends Servicio {

    @ManyToOne
    @JoinColumn(name = "id_tipo_vacuna", nullable = false)
    private TipoVacuna tipoVacuna;

    public ServicioVacunacion() {
    }

    public ServicioVacunacion(String nombre,
                              double precioBase,
                              int duracionMinutos,
                              TipoVacuna tipoVacuna) {

        super(nombre, precioBase, duracionMinutos);
        this.tipoVacuna = tipoVacuna;
    }

    public TipoVacuna getTipoVacuna() {
        return tipoVacuna;
    }

    public void setTipoVacuna(TipoVacuna tipoVacuna) {
        this.tipoVacuna = tipoVacuna;
    }

    @Override
    public void validar() {
        super.validar();
        if (tipoVacuna == null) {
            throw new IllegalArgumentException(
                    "Un servicio de vacunación debe tener un tipo de vacuna asociado."
            );
        }
    }

}