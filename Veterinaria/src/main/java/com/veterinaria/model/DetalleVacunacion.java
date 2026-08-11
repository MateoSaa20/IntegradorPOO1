package com.veterinaria.model;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("VACUNACION")
public class DetalleVacunacion extends DetalleAtencion {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vacuna", nullable = false)
    private TipoVacuna tipoVacuna;

    @Column(name = "laboratorio_o_marca")
    private String laboratorioOMarca; // Ej: "Zoetis", "Nobivac"

    @Column(name = "observaciones_dosis")
    private String observacionesDosis; // Ej: "1ra Dosis - Refuerzo a los 21 días"

    public DetalleVacunacion() {}

    public DetalleVacunacion(TipoVacuna tipoVacuna, String laboratorioOMarca, String observacionesDosis) {
        this.tipoVacuna = tipoVacuna;
        this.laboratorioOMarca = laboratorioOMarca;
        this.observacionesDosis = observacionesDosis;
    }

    // Getters y Setters
    public TipoVacuna getTipoVacuna() { return tipoVacuna; }
    public void setTipoVacuna(TipoVacuna tipoVacuna) { this.tipoVacuna = tipoVacuna; }

    public String getLaboratorioOMarca() { return laboratorioOMarca; }
    public void setLaboratorioOMarca(String laboratorioOMarca) { this.laboratorioOMarca = laboratorioOMarca; }

    public String getObservacionesDosis() { return observacionesDosis; }
    public void setObservacionesDosis(String observacionesDosis) { this.observacionesDosis = observacionesDosis; }
}