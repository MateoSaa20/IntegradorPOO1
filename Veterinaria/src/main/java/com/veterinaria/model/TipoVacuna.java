package com.veterinaria.model;

import com.veterinaria.util.TextoUtil;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tipos_vacuna")
public class TipoVacuna{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreComercial;

    @Column(nullable = false)
    private String enfermedadQuePreviene;

    @Column(nullable = false)
    private int periodicidadMeses;

    @Column(name = "es_ciclica", nullable = false)
    private boolean esCiclica;

    public TipoVacuna() {
    }

    public TipoVacuna(String nombreComercial,
                      String enfermedadQuePreviene,
                      int periodicidadMeses,
                      boolean esCiclica) {

        this.nombreComercial = nombreComercial;
        this.enfermedadQuePreviene = enfermedadQuePreviene;
        this.periodicidadMeses = periodicidadMeses;
        this.esCiclica = esCiclica;
    }

    public Long getId() {
        return id;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getEnfermedadQuePreviene() {
        return enfermedadQuePreviene;
    }

    public void setEnfermedadQuePreviene(String enfermedadQuePreviene) {
        this.enfermedadQuePreviene = enfermedadQuePreviene;
    }

    public int getPeriodicidadMeses() {
        return periodicidadMeses;
    }

    public void setPeriodicidadMeses(int periodicidadMeses) {
        this.periodicidadMeses = periodicidadMeses;
    }

    public boolean isEsCiclica() {
        return esCiclica;
    }

    public void setEsCiclica(boolean esCiclica) {
        this.esCiclica = esCiclica;
    }

    /**
     * Regla de negocio: calcula la fecha de la próxima aplicación de una
     * vacuna cíclica. Solo las vacunas cíclicas generan recordatorio: las
     * de dosis única o no periódicas devuelven null (no corresponde alertar).
     */
    public LocalDate calcularProximaAplicacion(LocalDate ultimaAplicacion) {
        if (!esCiclica || ultimaAplicacion == null) {
            return null;
        }
        return ultimaAplicacion.plusMonths(periodicidadMeses);
    }

    /**
     * Regla de negocio: valida los campos obligatorios del tipo de vacuna y
     * normaliza los textos a formato título.
     */
    public void validar() {
        if (nombreComercial == null || nombreComercial.isBlank()) {
            throw new IllegalArgumentException("El nombre comercial de la vacuna es obligatorio.");
        }
        if (enfermedadQuePreviene == null || enfermedadQuePreviene.isBlank()) {
            throw new IllegalArgumentException("Debe indicar la enfermedad que previene la vacuna.");
        }
        if (periodicidadMeses <= 0) {
            throw new IllegalArgumentException("La periodicidad debe ser mayor a cero.");
        }
        this.nombreComercial = TextoUtil.capitalizar(nombreComercial);
        this.enfermedadQuePreviene = TextoUtil.capitalizar(enfermedadQuePreviene);
    }

    /**
     * Regla de negocio: verifica si la fecha de una nueva aplicación cae
     * dentro de la ventana de periodicidad de la vacuna. Si la mascota ya
     * recibió la vacuna hace menos de periodicidadMeses, no corresponde
     * volver a aplicarla.
     */
    public boolean estaDentroDePeriodicidad(LocalDate ultimaAplicacion,
                                            LocalDate nuevaFecha) {

        if (ultimaAplicacion == null || nuevaFecha == null) {
            return false;
        }

        LocalDate proximaAplicacionValida =
                ultimaAplicacion.plusMonths(periodicidadMeses);

        return nuevaFecha.isBefore(proximaAplicacionValida);
    }
}