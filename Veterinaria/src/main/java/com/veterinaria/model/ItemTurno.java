package com.veterinaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "item_turno")
public class ItemTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long idItem;

    // Regla de Negocio: Se guarda el valor histórico del servicio en ese momento
    @Column(nullable = false)
    private Double precioCobrado;

    @Column(name = "duracion_cobrada", nullable = false)
    private Integer duracionCobrada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_turno", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    // 1. Declarar la variable en los atributos de la clase (arriba junto a los demás campos):
    @OneToOne(mappedBy = "itemTurno", cascade = CascadeType.ALL, orphanRemoval = true)
    private DetalleAtencion detalleAtencion;

    public ItemTurno() {}

    public ItemTurno(Servicio servicio, Turno turno) {
        this.servicio = servicio;
        this.turno = turno;
        // Congelamos el precio y duración actual del servicio
        this.precioCobrado = servicio.getPrecio();
        this.duracionCobrada = servicio.getDuracionMinutos();
    }

    // Getters y Setters
    public Long getIdItem() { return idItem; }
    public void setIdItem(Long idItem) { this.idItem = idItem; }

    public Double getPrecioCobrado() { return precioCobrado; }
    public void setPrecioCobrado(Double precioCobrado) { this.precioCobrado = precioCobrado; }

    public Integer getDuracionCobrada() { return duracionCobrada; }
    public void setDuracionCobrada(Integer duracionCobrada) { this.duracionCobrada = duracionCobrada; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }




    //Metodos de DetalleAtencion
    public DetalleAtencion getDetalleAtencion() {
    return detalleAtencion;
}

public void setDetalleAtencion(DetalleAtencion detalleAtencion) {
    this.detalleAtencion = detalleAtencion;

    if (detalleAtencion != null) {
        detalleAtencion.setItemTurno(this);
    }
}
    
    @Override
    public String toString() {
        return servicio.getNombre();
    }
}