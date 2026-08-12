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
    private int dni;

    @Column(nullable = false, length = 20)
    private int telefono;

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
                   int dni,
                   int telefono) {

        setNombre(nombre);
        setApellido(apellido);
        this.dni = dni;
        this.telefono = telefono;
    }

    public void agregarMascota(Mascota mascota) {
        mascotas.add(mascota);
        mascota.setCliente(this);
    }

    public void quitarMascota(Mascota mascota) {
        mascotas.remove(mascota);
        mascota.setCliente(null);
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

    public int getDni() {
        return dni;
    }

    public int getTelefono() {
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

    public void setDni(int dni) {
        this.dni = dni;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }
}