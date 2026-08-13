package com.veterinaria.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.veterinaria.util.TextoUtil;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, unique = true, length = 15)
    private String dni;

    @Column(nullable = false, length = 20)
    private String telefono;

    @OneToMany(
            mappedBy = "cliente",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Mascota> mascotas = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(String nombre,
                   String apellido,
                   String dni,
                   String telefono) {

        setNombre(nombre);
        setApellido(apellido);
        this.dni = dni;
        this.telefono = telefono;
    }

    public void agregarMascota(Mascota mascota) {
        mascotas.add(mascota);
        mascota.setCliente(this);
    }

    // ===== GETTERS =====

    public Long getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDni() {
        return dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public List<Mascota> getMascotas() {
        return mascotas;
    }
    @Override
    public String toString() {
        return nombre + " " + apellido;
    }

    // ===== SETTERS =====

    public void setNombre(String nombre) {
        this.nombre = TextoUtil.capitalizar(nombre);
    }

    public void setApellido(String apellido) {
        this.apellido = TextoUtil.capitalizar(apellido);
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}