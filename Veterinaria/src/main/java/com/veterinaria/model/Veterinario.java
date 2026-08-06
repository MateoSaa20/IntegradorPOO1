package com.veterinaria.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "veterinarios")
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_veterinario")
    private Long idVeterinario;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String matricula;

    @ManyToMany
    @JoinTable(
        name = "veterinario_especialidad",
        joinColumns = @JoinColumn(name = "id_veterinario"),
        inverseJoinColumns = @JoinColumn(name = "id_especialidad")
    )
    private List<Especialidad> especialidades = new ArrayList<>();

    public Veterinario() {
    }

    public Veterinario(String nombre,
                       String apellido,
                       String matricula) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.matricula = matricula;
    }

    public void agregarEspecialidad(Especialidad especialidad) {
        if (!especialidades.contains(especialidad)) {
            especialidades.add(especialidad);    
        }
    }

    public void quitarEspecialidad(Especialidad especialidad) {
        especialidades.remove(especialidad);
    }

    // Getters y setters

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMatricula() {
        return matricula;
    }

    public List<Especialidad> getEspecialidades() {
        return especialidades;
    }

    public Long getIdVeterinario() {
    return idVeterinario;
}

public void setIdVeterinario(Long idVeterinario) {
    this.idVeterinario = idVeterinario;
}

public void setNombre(String nombre) {
    this.nombre = nombre;
}

public String getApellido() {
    return apellido;
}

public void setApellido(String apellido) {
    this.apellido = apellido;
}

public void setMatricula(String matricula) {
    this.matricula = matricula;
}
}