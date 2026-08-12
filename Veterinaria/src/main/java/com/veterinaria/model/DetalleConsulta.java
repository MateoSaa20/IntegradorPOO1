package com.veterinaria.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "detalles_consulta")
public class DetalleConsulta extends DetalleAtencion {

    @Column(nullable = false, length = 500)
    private String diagnostico;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_detalle")
    private List<Tratamiento> tratamientos = new ArrayList<>();

    public DetalleConsulta() {
    }

    public DetalleConsulta(String observaciones,
                           String diagnostico) {

        super(observaciones);
        this.diagnostico = diagnostico;
    }

   public void agregarTratamiento(Tratamiento tratamiento) {
    tratamientos.add(tratamiento);
}

public void quitarTratamiento(Tratamiento tratamiento) {
    tratamientos.remove(tratamiento);
}

    // ===== GETTERS =====

    public String getDiagnostico() {
        return diagnostico;
    }

    public List<Tratamiento> getTratamientos() {
        return tratamientos;
    }

    // ===== SETTERS =====

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public void setTratamientos(List<Tratamiento> tratamientos) {
        this.tratamientos.clear();
        if (tratamientos != null) {
            this.tratamientos.addAll(tratamientos);
        }
    }

    /**
     * Regla de negocio: el detalle de una consulta exige el diagnóstico
     * (las observaciones y los tratamientos son opcionales).
     */
    @Override
    public void validar() {
        if (diagnostico == null || diagnostico.isBlank()) {
            throw new IllegalArgumentException(
                    "El detalle de la consulta exige un diagnóstico."
            );
        }
    }
}
