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

    // Getters y Setters
}
