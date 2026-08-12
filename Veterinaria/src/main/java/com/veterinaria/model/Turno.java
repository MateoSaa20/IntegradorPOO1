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
                .mapToInt(ItemTurno::getTiempoAlMomento)
                .sum();
    }

    public double calcularPrecioTotal() {
        return items.stream()
                .mapToDouble(ItemTurno::getPrecioAlMomento)
                .sum();
    }
    //Metodos de restricción de turnos
    /**
     * Regla de negocio: un turno no puede agendarse en un día u hora
     * anterior (o igual) a la fecha y hora actual.
     */
    public void validarFechaFutura(LocalDateTime ahora) throws Exception {
        if (fechaHora == null || !fechaHora.isAfter(ahora)) {
            throw new Exception("Error: El turno debe agendarse para un día y hora futuros a la fecha y hora actual.");
        }
    }

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
                    minutosOcupado += item.getTiempoAlMomento(); 
                }
            }
        }
        
        return this.fechaHora.plusMinutes(minutosOcupado);
    }

    /**
     * Calcula hasta qué hora la mascota está ocupada. A diferencia del
     * veterinario, aquí TODOS los servicios cuentan (la guardería ocupa
     * a la mascota aunque no al veterinario). Como el ingreso de la
     * guardería coincide con el inicio del turno, sumar los minutos de
     * cada item (guardería incluida) cubre también las estadías de varios días.
     */
    public LocalDateTime calcularFinOcupacionMascota() {
        int minutosOcupado = 0;

        if (this.items != null) {
            for (ItemTurno item : this.items) {
                minutosOcupado += item.getTiempoAlMomento();
            }
        }

        return this.fechaHora.plusMinutes(minutosOcupado);
    }

    /**
     * Comprueba si este turno choca en horario con otro turno dado.
     */
    public boolean seSuperponeCon(Turno otroTurno) {
        return seSuperponeRangos(
                this.fechaHora,
                this.calcularFinOcupacionVeterinario(),
                otroTurno.getFechaHora(),
                otroTurno.calcularFinOcupacionVeterinario()
        );
    }

    /**
     * Comprueba si la mascota de este turno choca en horario con la de otro
     * turno, considerando la ocupación real de la mascota (guardería incluida).
     */
    public boolean seSuperponeMascotaCon(Turno otroTurno) {
        return seSuperponeRangos(
                this.fechaHora,
                this.calcularFinOcupacionMascota(),
                otroTurno.getFechaHora(),
                otroTurno.calcularFinOcupacionMascota()
        );
    }

    private static boolean seSuperponeRangos(LocalDateTime inicioA,
                                             LocalDateTime finA,
                                             LocalDateTime inicioB,
                                             LocalDateTime finB) {
        // Si alguno no ocupa tiempo (ej. solo guardería para el veterinario),
        // no hay superposición para esa persona/animal.
        if (inicioA == null || finA == null || inicioB == null || finB == null) {
            return false;
        }
        if (inicioA.isEqual(finA) || inicioB.isEqual(finB)) {
            return false;
        }

        // Fórmula de solapamiento de rangos de tiempo
        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }

    /**
     * Valida el turno actual contra una lista de turnos existentes del mismo día.
     * Lanza excepción si encuentra un solapamiento.
     * Los turnos CANCELADOS liberan el horario y no bloquean el agendamiento.
     */
    public void validarDisponibilidad(List<Turno> turnosDelDia) throws Exception {
        for (Turno turnoExistente : turnosDelDia) {
            if (turnoExistente.getEstado() == EstadoTurno.CANCELADO) {
                continue;
            }
            if (this.seSuperponeCon(turnoExistente)) {
                throw new Exception("Error: El turno se superpone con otro agendado para este veterinario " +
                                    "desde las " + turnoExistente.getFechaHora().toLocalTime() + 
                                    " hasta las " + turnoExistente.calcularFinOcupacionVeterinario().toLocalTime());
            }
        }
    }

    /**
     * Valida que la mascota no tenga otro turno en el mismo horario (con
     * cualquier veterinario). Los turnos CANCELADOS liberan a la mascota.
     */
    public void validarDisponibilidadMascota(List<Turno> turnosDeLaMascota) throws Exception {
        for (Turno turnoExistente : turnosDeLaMascota) {
            if (turnoExistente.getEstado() == EstadoTurno.CANCELADO) {
                continue;
            }
            if (this.seSuperponeMascotaCon(turnoExistente)) {
                throw new Exception("Error: La mascota ya tiene un turno en ese horario " +
                                    "desde las " + turnoExistente.getFechaHora().toLocalTime() +
                                    " hasta las " + turnoExistente.calcularFinOcupacionMascota().toLocalTime());
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