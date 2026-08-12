package com.veterinaria.model;

import com.veterinaria.util.TextoUtil;
import jakarta.persistence.*;

@Entity
@Table(name = "servicios")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_servicio", discriminatorType = DiscriminatorType.STRING)
public abstract class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long idServicio;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    public Servicio() {}

    public Servicio(String nombre, Double precioBase, Integer duracionMinutos) {
        this.nombre = nombre;
        this.precio = precioBase;
        this.duracionMinutos = duracionMinutos;
    }

    // Getters y Setters
    public Long getIdServicio() { return idServicio; }
    public void setIdServicio(Long idServicio) { this.idServicio = idServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }

    /**
     * Regla de negocio: subtotal cobrado por el servicio.
     * Para la mayoría de los servicios es el precio base. La guardería
     * cobra por cantidad de días y por eso expone su propia variante
     * con rango de fechas (ver ServicioGuarderia.calcularSubtotalPorDias).
     */
    public double calcularSubtotal() {
        return precio;
    }

    /**
     * Regla de negocio: valida los campos comunes de un servicio y normaliza
     * el nombre a formato título.
     */
    public void validar() {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del servicio es obligatorio.");
        }
        if (precio == null || precio <= 0) {
            throw new IllegalArgumentException("El precio del servicio debe ser mayor a cero.");
        }
        if (duracionMinutos == null || duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración del servicio debe ser mayor a cero.");
        }
        this.nombre = TextoUtil.capitalizar(nombre);
    }

    @Override
    public String toString() {
        return nombre;
    }
}