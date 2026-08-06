package com.veterinaria.model;

import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.*;

@Entity
@Table(name = "razas")
public class Raza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "especie_id")
    private Especie especie;

    @OneToMany(mappedBy = "raza")
    private List<Mascota> mascotas = new ArrayList<>();

    public Raza() {
    }

    public Raza(String nombre, Especie especie) {
        this.nombre = nombre;
        this.especie = especie;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }
}