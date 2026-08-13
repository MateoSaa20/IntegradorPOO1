package com.veterinaria.controller;

import com.veterinaria.model.Cliente;
import com.veterinaria.model.DetalleVacunacion;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.TipoVacuna;
import com.veterinaria.model.Turno;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.TurnoRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Orquesta la consulta del historial y de las vacunas de las mascotas:
 * - Busca al dueño por DNI.
 * - Lista sus mascotas, las atenciones realizadas y las vacunas aplicadas.
 * - Calcula el estado de cada vacuna cíclica (al día / por vencer / vencida)
 *   a partir de la última aplicación y la periodicidad del tipo de vacuna.
 */
public class HistorialController {

    private final ClienteRepository clienteRepository;
    private final TurnoRepository turnoRepository;

    public HistorialController(EntityManager em) {
        this.clienteRepository = new ClienteRepository(em);
        this.turnoRepository = new TurnoRepository(em);
    }

    public Optional<Cliente> buscarClientePorDni(String dni) {
        if (dni == null) {
            return Optional.empty();
        }
        String normalizado = dni.trim().replace(".", "");
        if (normalizado.isEmpty()) {
            return Optional.empty();
        }
        return clienteRepository.buscarPorDni(normalizado);
    }

    public List<Mascota> listarMascotas(Cliente cliente) {
        return cliente.getMascotas();
    }

    public List<Turno> listarAtenciones(Mascota mascota) {
        return turnoRepository.listarTurnosAtendidosDeMascota(mascota.getNumeroFicha());
    }

    public List<DetalleVacunacion> listarVacunas(Mascota mascota) {
        return turnoRepository.listarVacunasAplicadasDeMascota(mascota.getNumeroFicha());
    }

    /**
     * Por cada tipo de vacuna aplicado a la mascota toma la última fecha de
     * aplicación y calcula la próxima dosis (solo para vacunas cíclicas).
     * Ordena primero las que requieren atención (por vencer/vencidas).
     */
    public List<EstadoVacuna> calcularEstadoVacunas(Mascota mascota) {
        Map<TipoVacuna, LocalDate> ultimaPorTipo = new LinkedHashMap<>();

        for (DetalleVacunacion vacuna : listarVacunas(mascota)) {
            if (vacuna.getTipoVacuna() == null
                    || vacuna.getItemTurno() == null
                    || vacuna.getItemTurno().getTurno() == null) {
                continue;
            }

            LocalDate aplicacion = vacuna.getItemTurno()
                    .getTurno().getFechaHora().toLocalDate();

            ultimaPorTipo.merge(
                    vacuna.getTipoVacuna(),
                    aplicacion,
                    (prev, nuevo) -> prev.isAfter(nuevo) ? prev : nuevo
            );
        }

        List<EstadoVacuna> resultado = new ArrayList<>();
        for (Map.Entry<TipoVacuna, LocalDate> entry : ultimaPorTipo.entrySet()) {
            TipoVacuna tipo = entry.getKey();
            resultado.add(new EstadoVacuna(
                    tipo,
                    entry.getValue(),
                    tipo.calcularProximaAplicacion(entry.getValue())
            ));
        }

        resultado.sort(
                Comparator.comparing(EstadoVacuna::porVencer).reversed()
                        .thenComparing(EstadoVacuna::diasParaProxima)
        );

        return resultado;
    }

    public List<EstadoVacuna> vacunasPorVencer(Mascota mascota) {
        return calcularEstadoVacunas(mascota).stream()
                .filter(EstadoVacuna::porVencer)
                .toList();
    }
}
