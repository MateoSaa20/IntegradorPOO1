package com.veterinaria.service;

import com.veterinaria.model.TipoVacuna;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DTO de solo lectura (NO es una entidad) que resume el estado de una vacuna
 * cíclica para una mascota: cuándo se aplicó la última dosis y cuándo toca la
 * próxima. Permite decidir si corresponde alertar porque falta menos de un mes.
 */
public record EstadoVacuna(
        TipoVacuna tipoVacuna,
        LocalDate ultimaAplicacion,
        LocalDate proximaAplicacion
) {

    public static final long MES_AVISO_DIAS = 30;

    public String getNombreVacuna() {
        return tipoVacuna.getNombreComercial();
    }

    public String getEnfermedad() {
        return tipoVacuna.getEnfermedadQuePreviene();
    }

    /**
     * Días que faltan para la próxima dosis. Negativo si ya venció.
     */
    public long diasParaProxima() {
        if (proximaAplicacion == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), proximaAplicacion);
    }

    public boolean vencida() {
        return proximaAplicacion != null && diasParaProxima() < 0;
    }

    /**
     * Falta menos de un mes para la próxima dosis (incluye las vencidas).
     */
    public boolean porVencer() {
        return proximaAplicacion != null && diasParaProxima() <= MES_AVISO_DIAS;
    }

    public String getEstado() {
        if (proximaAplicacion == null) {
            return "No aplica";
        }
        if (vencida()) {
            return "Vencida";
        }
        if (porVencer()) {
            return "Por vencer";
        }
        return "Al día";
    }
}
