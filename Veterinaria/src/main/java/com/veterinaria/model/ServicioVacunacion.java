package com.veterinaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "servicios_vacunacion")
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

}