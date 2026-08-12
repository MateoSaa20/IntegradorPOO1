package com.veterinaria.model;

import jakarta.persistence.*;

@Entity
@Table(name = "detalles_atencion")
@Inheritance(strategy = InheritanceType.JOINED)
public class DetalleAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @Column(length = 500)
    private String observaciones;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item_turno", unique = true)
    private ItemTurno itemTurno;

    public DetalleAtencion() {
    }

    public DetalleAtencion(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public ItemTurno getItemTurno() {
        return itemTurno;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public void setItemTurno(ItemTurno itemTurno) {
        this.itemTurno = itemTurno;
    }

    /**
     * Regla de negocio: un detalle cargado no puede quedar sin contenido.
     * Para los servicios sin campos específicos (guardería, peluquería)
     * las observaciones son obligatorias.
     */
    public void validar() {
        if (observaciones == null || observaciones.isBlank()) {
            throw new IllegalArgumentException(
                    "El detalle del servicio no puede estar vacío: indique las observaciones."
            );
        }
    }
}