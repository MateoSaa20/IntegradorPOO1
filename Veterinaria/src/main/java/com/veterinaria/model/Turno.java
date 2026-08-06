package com.veterinaria.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "turnos")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Long idTurno;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_veterinario", nullable = false)
    private Veterinario veterinario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numero_ficha", nullable = false)
    private Mascota mascota;

    @OneToMany(
        mappedBy = "turno",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<ItemTurno> items = new ArrayList<>();

    public Turno() {
    }

    public Turno(LocalDateTime fechaHora,
                 EstadoTurno estado,
                 Veterinario veterinario,
                 Mascota mascota) {

        this.fechaHora = fechaHora;
        this.estado = estado;
        this.veterinario = veterinario;
        this.mascota = mascota;
    }

    public void agregarItem(ItemTurno item){
        items.add(item);
        item.setTurno(this);
    }

    public void eliminarItem(ItemTurno item){
        items.remove(item);
        item.setTurno(null);
    }

    public int calcularTiempoTotal() {
        return items.stream()
                .mapToInt(ItemTurno::getTiempoAlMomento)
                .sum();
    }

    public double calcularPrecioTotal() {
        return items.stream()
                .mapToDouble(ItemTurno::getPrecioAlMomento)
                .sum();
    }

    // Getters y Setters
    public Long getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(Long idTurno) {
        this.idTurno = idTurno;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    public Veterinario getVeterinario() {
        return veterinario;
    }

    public void setVeterinario(Veterinario veterinario) {
        this.veterinario = veterinario;
    }

    public Mascota getMascota() {
        return mascota;
    }

    public void setMascota(Mascota mascota) {
        this.mascota = mascota;
    }

    public List<ItemTurno> getItems() {
        return items;
    }

    public void setItems(List<ItemTurno> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Turno " + idTurno;
    }
}