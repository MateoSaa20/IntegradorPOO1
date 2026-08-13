package com.veterinaria.controller;

import com.veterinaria.model.Mascota;

/**
 * DTO de solo lectura (NO es una entidad) para la pantalla de control de
 * vacunaciones: asocia una mascota con una de sus vacunas que requiere
 * atención (vencida o por vencer dentro del mes de aviso).
 */
public record AlertaVacunacion(
        Mascota mascota,
        EstadoVacuna estado
) {

    public String getMascotaNombre() {
        return mascota.getNombre();
    }

    public String getEspecie() {
        return mascota.getEspecie().getNombre();
    }

    public String getDueno() {
        return mascota.getCliente() != null
                ? mascota.getCliente().getNombre() + " " + mascota.getCliente().getApellido()
                : "";
    }
}
