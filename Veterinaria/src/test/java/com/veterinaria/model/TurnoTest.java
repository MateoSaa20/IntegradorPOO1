package com.veterinaria.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TurnoTest {

    private Turno turnoBase;
    private ServicioConsulta servicioConsulta;
    private ServicioGuarderia servicioGuarderia;

    @BeforeEach
    public void setUp() {
        // Inicializamos objetos básicos antes de cada prueba
        // (Ajusta los constructores o setters según tu código real)
        servicioConsulta = new ServicioConsulta();
        servicioGuarderia = new ServicioGuarderia();
        
        turnoBase = new Turno();
        turnoBase.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0)); // Turno a las 10:00 AM
        turnoBase.setItems(new ArrayList<>());
    }

    @Test
    public void testCalcularFinOcupacion_SoloServiciosComunes() {
        // Preparación: Un turno con 2 consultas de 30 mins cada una
        ItemTurno item1 = new ItemTurno();
        item1.setServicio(servicioConsulta);
        item1.setTiempoAlMomento(30);

        ItemTurno item2 = new ItemTurno();
        item2.setServicio(servicioConsulta);
        item2.setTiempoAlMomento(30);

        turnoBase.getItems().add(item1);
        turnoBase.getItems().add(item2);

        // Ejecución
        LocalDateTime finCalculado = turnoBase.calcularFinOcupacionVeterinario();

        // Verificación: Debe terminar a las 11:00 AM (60 minutos después)
        assertEquals(LocalDateTime.of(2026, 8, 10, 11, 0), finCalculado);
    }

    @Test
    public void testCalcularFinOcupacion_IgnoraGuarderia() {
        // Preparación: Un turno con una consulta (30 min) y guardería (120 min)
        ItemTurno itemConsulta = new ItemTurno();
        itemConsulta.setServicio(servicioConsulta);
        itemConsulta.setTiempoAlMomento(30);

        ItemTurno itemGuarderia = new ItemTurno();
        itemGuarderia.setServicio(servicioGuarderia);
        itemGuarderia.setTiempoAlMomento(120); 

        turnoBase.getItems().add(itemConsulta);
        turnoBase.getItems().add(itemGuarderia);

        // Ejecución
        LocalDateTime finCalculado = turnoBase.calcularFinOcupacionVeterinario();

        // Verificación: Solo debe sumar los 30 min de la consulta. La guardería se ignora.
        assertEquals(LocalDateTime.of(2026, 8, 10, 10, 30), finCalculado);
    }

    @Test
    public void testSeSuperponeCon_SuperposicionReal() {
        // Turno A: de 10:00 a 11:00
        ItemTurno itemA = new ItemTurno();
        itemA.setServicio(servicioConsulta);
        itemA.setTiempoAlMomento(60);
        turnoBase.getItems().add(itemA);

        // Turno B: de 10:30 a 11:30 (Choca con el Turno A)
        Turno turnoB = new Turno();
        turnoB.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 30));
        turnoB.setItems(new ArrayList<>());
        ItemTurno itemB = new ItemTurno();
        itemB.setServicio(servicioConsulta);
        itemB.setTiempoAlMomento(60);
        turnoB.getItems().add(itemB);

        // Verificación
        assertTrue(turnoBase.seSuperponeCon(turnoB));
        assertTrue(turnoB.seSuperponeCon(turnoBase)); // Debe ser bidireccional
    }

    @Test
    public void testSeSuperponeCon_TurnosConsecutivosSinChoque() {
        // Turno A: de 10:00 a 11:00
        ItemTurno itemA = new ItemTurno();
        itemA.setServicio(servicioConsulta);
        itemA.setTiempoAlMomento(60);
        turnoBase.getItems().add(itemA);

        // Turno B: de 11:00 a 12:00 (Empieza justo cuando termina el A)
        Turno turnoB = new Turno();
        turnoB.setFechaHora(LocalDateTime.of(2026, 8, 10, 11, 0));
        turnoB.setItems(new ArrayList<>());
        ItemTurno itemB = new ItemTurno();
        itemB.setServicio(servicioConsulta);
        itemB.setTiempoAlMomento(60);
        turnoB.getItems().add(itemB);

        // Verificación: No deben superponerse
        assertFalse(turnoBase.seSuperponeCon(turnoB));
    }

    @Test
    public void testValidarDisponibilidad_LanzaExcepcion() {
        // Turno existente: 10:00 a 11:00
        ItemTurno itemA = new ItemTurno();
        itemA.setServicio(servicioConsulta);
        itemA.setTiempoAlMomento(60);
        turnoBase.getItems().add(itemA);

        List<Turno> turnosDelDia = new ArrayList<>();
        turnosDelDia.add(turnoBase);

        // Nuevo turno: 10:45 a 11:15 (Se superpone)
        Turno nuevoTurno = new Turno();
        nuevoTurno.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 45));
        nuevoTurno.setItems(new ArrayList<>());
        ItemTurno itemNuevo = new ItemTurno();
        itemNuevo.setServicio(servicioConsulta);
        itemNuevo.setTiempoAlMomento(30);
        nuevoTurno.getItems().add(itemNuevo);

        // Verificación: Se espera que lance Exception
        Exception exception = assertThrows(Exception.class, () -> {
            nuevoTurno.validarDisponibilidad(turnosDelDia);
        });

        assertTrue(exception.getMessage().contains("se superpone"));
    }
    @Test
    public void testTransicionesDeEstadoExitosas() throws Exception {
        turnoBase.setEstado(EstadoTurno.PENDIENTE);
        
        // Pendiente -> Confirmado (Debe funcionar)
        turnoBase.confirmar();
        assertEquals(EstadoTurno.CONFIRMADO, turnoBase.getEstado());
        
        // Confirmado -> Atendido (Debe funcionar)
        turnoBase.atender();
        assertEquals(EstadoTurno.ATENDIDO, turnoBase.getEstado());
    }

    @Test
    public void testAtenderFallaSiNoEstaConfirmado() {
        // Turno en pendiente que intenta pasar directo a atendido
        turnoBase.setEstado(EstadoTurno.PENDIENTE);
        
        Exception exception = assertThrows(Exception.class, () -> {
            turnoBase.atender();
        });
        assertTrue(exception.getMessage().contains("no ha sido CONFIRMADO"));
    }

    @Test
    public void testCancelarDesdePendiente_Exitoso() throws Exception {
        turnoBase.setEstado(EstadoTurno.PENDIENTE);
        
        // La fecha actual no importa mucho aquí, pero pasamos una cualquiera
        turnoBase.cancelar(LocalDateTime.now()); 
        
        assertEquals(EstadoTurno.CANCELADO, turnoBase.getEstado());
    }

    @Test
    public void testCancelarDesdeConfirmado_ConAnticipacion_Exitoso() throws Exception {
        turnoBase.setEstado(EstadoTurno.CONFIRMADO);
        turnoBase.setFechaHora(LocalDateTime.of(2026, 8, 20, 10, 0)); // Turno el 20 de Agosto a las 10hs
        
        // Intentamos cancelar el 18 de Agosto (Más de 24 horas de anticipación)
        LocalDateTime momentoCancelacion = LocalDateTime.of(2026, 8, 18, 10, 0);
        turnoBase.cancelar(momentoCancelacion);
        
        assertEquals(EstadoTurno.CANCELADO, turnoBase.getEstado());
    }

    @Test
    public void testCancelarDesdeConfirmado_SinAnticipacion_LanzaExcepcion() {
        turnoBase.setEstado(EstadoTurno.CONFIRMADO);
        turnoBase.setFechaHora(LocalDateTime.of(2026, 8, 20, 10, 0)); // Turno el 20 de Agosto a las 10hs
        
        // Intentamos cancelar el 19 de Agosto a las 15hs (Quedan menos de 24hs)
        LocalDateTime momentoCancelacion = LocalDateTime.of(2026, 8, 19, 15, 0);
        
        Exception exception = assertThrows(Exception.class, () -> {
            turnoBase.cancelar(momentoCancelacion);
        });
        assertTrue(exception.getMessage().contains("al menos 24 horas de anticipación"));
    }
}