package com.veterinaria.model;

import java.time.LocalDate;

import jakarta.persistence.*;

import com.veterinaria.util.TextoUtil;

@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numero_ficha")
    private Long numeroFicha;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_raza", nullable = false)
    private Raza raza;

    public Mascota() {
    }

    public Mascota(String nombre,
                   LocalDate fechaNacimiento,
                   Sexo sexo,
                   Raza raza) {

        setNombre(nombre);
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.raza = raza;
    }

    // ===== Getters =====

    public Long getNumeroFicha() {
        return numeroFicha;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Raza getRaza() {
        return raza;
    }

    public Especie getEspecie() {
        return raza.getEspecie();
    }

    // ===== Setters =====

    public void setNombre(String nombre) {
        this.nombre = TextoUtil.capitalizar(nombre);
    }

    public void setEspecie(Especie especie) {
        if (this.raza != null) {
            this.raza.setEspecie(especie);
        }
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setRaza(Raza raza) {
        this.raza = raza;
    }

    public void validarFechaNacimiento() {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser posterior a la fecha actual.");
        }
    }

    @Override
    public String toString() {
        return nombre + " (" + numeroFicha + ")";
    }
}
