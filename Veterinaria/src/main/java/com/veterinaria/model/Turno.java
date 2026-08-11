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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_veterinario", nullable = false)
    private Veterinario veterinario;

    @ManyToOne(fetch = FetchType.EAGER)
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
                .mapToInt(ItemTurno::getDuracionCobrada)
                .sum();
    }

    public double calcularPrecioTotal() {
        return items.stream()
                .mapToDouble(ItemTurno::getPrecioCobrado)
                .sum();
    }
    //Metodos de restricción de turnos
    /**
     * Calcula a qué hora se libera el veterinario sumando el 'tiempoAlMomento'
     * de los ItemTurno, excluyendo servicios como Guardería.
     */
    public LocalDateTime calcularFinOcupacionVeterinario() {
        int minutosOcupado = 0;
        
        if (this.items != null) {
            for (ItemTurno item : this.items) {
                // Excluimos la guardería porque el veterinario no se queda con el animal
                if (!(item.getServicio() instanceof ServicioGuarderia)) {
                    // Usamos el tiempo guardado en el detalle del turno
                    minutosOcupado += item.getDuracionCobrada(); 
                }
            }
        }
        
        return this.fechaHora.plusMinutes(minutosOcupado);
    }

    /**
     * Comprueba si este turno choca en horario con otro turno dado.
     */
    public boolean seSuperponeCon(Turno otroTurno) {
        LocalDateTime miInicio = this.fechaHora;
        LocalDateTime miFin = this.calcularFinOcupacionVeterinario();

        LocalDateTime otroInicio = otroTurno.getFechaHora();
        LocalDateTime otroFin = otroTurno.calcularFinOcupacionVeterinario();

        // Si mi tiempo de ocupación es cero (ej. solo guardería), no hay superposición para el veterinario
        if (miInicio.isEqual(miFin) || otroInicio.isEqual(otroFin)) {
            return false; 
        }

        // Fórmula de solapamiento de rangos de tiempo
        return miInicio.isBefore(otroFin) && miFin.isAfter(otroInicio);
    }

    /**
     * Valida el turno actual contra una lista de turnos existentes del mismo día.
     * Lanza excepción si encuentra un solapamiento.
     */
    public void validarDisponibilidad(List<Turno> turnosDelDia) throws Exception {
        for (Turno turnoExistente : turnosDelDia) {
            if (this.seSuperponeCon(turnoExistente)) {
                throw new Exception("Error: El turno se superpone con otro agendado para este veterinario " +
                                    "desde las " + turnoExistente.getFechaHora().toLocalTime() + 
                                    " hasta las " + turnoExistente.calcularFinOcupacionVeterinario().toLocalTime());
            }
        }
    }

    //Para restricción de cancelación de turnos 
    /**
     * Pasa el turno de PENDIENTE a CONFIRMADO.
     */
    public void confirmar() throws Exception {
        if (this.estado != EstadoTurno.PENDIENTE) {
            throw new Exception("Error: Solo se puede confirmar un turno que actualmente está PENDIENTE.");
        }
        this.estado = EstadoTurno.CONFIRMADO;
    }

    /**
     * Pasa el turno de CONFIRMADO a ATENDIDO.
     */
    public void atender() throws Exception {
        if (this.estado != EstadoTurno.CONFIRMADO) {
            throw new Exception("Error: El turno no puede pasar a ATENDIDO porque no ha sido CONFIRMADO previamente.");
        }
        this.estado = EstadoTurno.ATENDIDO;
    }

    /**
     * Cancela el turno validando las reglas de estado y tiempo de anticipación.
     * @param fechaActual La fecha y hora exacta en la que se solicita la cancelación.
     */
    public void cancelar(LocalDateTime fechaActual) throws Exception {
        // Desde confirmado y pendiente, requiere 24h de anticipación
        if (this.estado == EstadoTurno.CONFIRMADO || this.estado == EstadoTurno.PENDIENTE) {
            // Calculamos cuál era el límite para cancelar (24hs antes del turno)
            LocalDateTime limiteCancelacion = this.fechaHora.minusHours(24);
            // Si la fecha actual ya superó el límite, rechazamos la cancelación
            if (fechaActual.isAfter(limiteCancelacion)) {
                throw new Exception("Error: Los solo pueden cancelarse con al menos 24 horas de anticipación.");
            }       
            this.estado = EstadoTurno.CANCELADO;
            return;
        }
        // Si está en ATENDIDO u otro estado futuro, no se puede cancelar
        throw new Exception("Error: No se puede cancelar un turno que está en estado " + this.estado);
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