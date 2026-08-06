package com.veterinaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "items_turno")
public class ItemTurno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_turno")
    private Long idItemTurno;

    @Column(nullable = false)
    private double precioAlMomento;

    @Column(nullable = false)
    private int tiempoAlMomento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_turno", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @OneToOne(
        mappedBy = "itemTurno",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private DetalleAtencion detalleAtencion;

    public ItemTurno() {
    }

    public ItemTurno(Servicio servicio) {
        this.servicio = servicio;
        this.precioAlMomento = servicio.getPrecioBase();
        this.tiempoAlMomento = servicio.getDuracionMinutos();
    }

    public Long getIdItemTurno() {
        return idItemTurno;
    }

    public double getPrecioAlMomento() {
        return precioAlMomento;
    }

    public int getTiempoAlMomento() {
        return tiempoAlMomento;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

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