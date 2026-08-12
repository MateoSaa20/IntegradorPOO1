package com.veterinaria.model;

import com.veterinaria.util.TextoUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReglasNegocioTest {

    private ServicioGuarderia guarderia(double precio, int duracionMinutos) {
        return new ServicioGuarderia("Guardería", precio, duracionMinutos);
    }

    // ==========================================================
    // GUARDERÍA: PRECIO POR CANTIDAD DE DÍAS
    // ==========================================================

    @Test
    public void guarderiaMismoDiaCobraUnDia() {
        ServicioGuarderia servicio = guarderia(1000, 1440);
        Turno turno = new Turno();

        ItemGuarderia item = new ItemGuarderia(
                servicio,
                turno,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 10, 18, 0)
        );

        assertEquals(1, item.calcularCantidadDias());
        assertEquals(1000.0, item.getPrecioAlMomento());
    }

    @Test
    public void guarderiaSalidaDiaSiguienteCobraDosDias() {
        ServicioGuarderia servicio = guarderia(1000, 1440);
        Turno turno = new Turno();

        ItemGuarderia item = new ItemGuarderia(
                servicio,
                turno,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 11, 9, 0)
        );

        assertEquals(2, item.calcularCantidadDias());
        assertEquals(2000.0, item.getPrecioAlMomento());
    }

    @Test
    public void guarderiaTresDiasCobraPrecioPorTres() {
        ServicioGuarderia servicio = guarderia(1000, 1440);
        Turno turno = new Turno();

        ItemGuarderia item = new ItemGuarderia(
                servicio,
                turno,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 12, 9, 0)
        );

        assertEquals(3, item.calcularCantidadDias());
        assertEquals(3000.0, item.getPrecioAlMomento());
    }

    @Test
    public void servicioGuarderiaCalculaSubtotalPorDias() {
        ServicioGuarderia servicio = guarderia(18000, 1440);

        double subtotal = servicio.calcularSubtotalPorDias(
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 12, 18, 0)
        );

        // 10/08 -> 12/08 = 3 días
        assertEquals(54000.0, subtotal);
    }

    @Test
    public void servicioGuarderiaSinRangoDevuelvePrecioBase() {
        ServicioGuarderia servicio = guarderia(18000, 1440);

        assertEquals(18000.0, servicio.calcularSubtotalPorDias(null, null));
    }

    @Test
    public void servicioComunCalculaSuPrecioBaseSinRango() {
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 30);

        assertEquals(6500.0, consulta.calcularSubtotal());
    }

    // ==========================================================
    // GUARDERÍA: VALIDACIÓN DE RANGO
    // ==========================================================

    @Test
    public void guarderiaNoPuedeIniciarAntesDeLaActual() {
        ServicioGuarderia servicio = guarderia(1000, 1440);
        Turno turno = new Turno();

        ItemGuarderia item = new ItemGuarderia(
                servicio,
                turno,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 11, 9, 0)
        );

        Exception exception = assertThrows(Exception.class, () ->
                item.validarRango(LocalDateTime.of(2026, 8, 10, 10, 0))
        );

        assertTrue(exception.getMessage().contains("no puede iniciar"));
    }

    @Test
    public void guarderiaSalidaDebeSerPosteriorAlIngreso() {
        ServicioGuarderia servicio = guarderia(1000, 1440);
        Turno turno = new Turno();

        ItemGuarderia item = new ItemGuarderia(
                servicio,
                turno,
                LocalDateTime.of(2026, 8, 10, 18, 0),
                LocalDateTime.of(2026, 8, 10, 9, 0)
        );

        Exception exception = assertThrows(Exception.class, () ->
                item.validarRango(LocalDateTime.of(2026, 8, 10, 8, 0))
        );

        assertTrue(exception.getMessage().contains("posterior al ingreso"));
    }

    @Test
    public void guarderiaRangoValidoNoLanzaExcepcion() throws Exception {
        ServicioGuarderia servicio = guarderia(1000, 1440);
        Turno turno = new Turno();

        ItemGuarderia item = new ItemGuarderia(
                servicio,
                turno,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 11, 9, 0)
        );

        item.validarRango(LocalDateTime.of(2026, 8, 9, 10, 0));
    }

    // ==========================================================
    // GUARDERÍA: NO CUENTA EN LA OCUPACIÓN DEL VETERINARIO
    // ==========================================================

    @Test
    public void turnoSoloConGuarderiaNoSeSuperponeConOtro() {
        ServicioGuarderia servicio = guarderia(1000, 1440);
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 60);

        // Turno A: solo guardería (el veterinario no queda ocupado)
        Turno turnoA = new Turno();
        turnoA.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        turnoA.agregarItem(new ItemGuarderia(
                servicio,
                turnoA,
                LocalDateTime.of(2026, 8, 10, 10, 0),
                LocalDateTime.of(2026, 8, 11, 10, 0)
        ));

        // Turno B: consulta de 60 min a la misma hora
        Turno turnoB = new Turno();
        turnoB.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        ItemTurno itemB = new ItemTurno(consulta, turnoB);
        itemB.setTiempoAlMomento(60);
        turnoB.agregarItem(itemB);

        assertFalse(turnoA.seSuperponeCon(turnoB));
        assertFalse(turnoB.seSuperponeCon(turnoA));
    }

    @Test
    public void turnoConConsultaYSoloGuarderiaNoGeneraSolapamiento() {
        ServicioGuarderia servicio = guarderia(1000, 1440);
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 30);

        // Turno A: consulta 10:00-10:30
        Turno turnoA = new Turno();
        turnoA.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        turnoA.agregarItem(new ItemTurno(consulta, turnoA));

        // Turno B: consulta 10:30-11:00 (consecutivo, no choca) + guardería
        Turno turnoB = new Turno();
        turnoB.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 30));
        turnoB.agregarItem(new ItemTurno(consulta, turnoB));
        turnoB.agregarItem(new ItemGuarderia(
                servicio,
                turnoB,
                LocalDateTime.of(2026, 8, 10, 10, 30),
                LocalDateTime.of(2026, 8, 11, 10, 30)
        ));

        assertFalse(turnoA.seSuperponeCon(turnoB));
    }

    // ==========================================================
    // TURNO: FECHA FUTURA
    // ==========================================================

    @Test
    public void turnoEnElPasadoLanzaExcepcion() {
        Turno turno = new Turno();
        turno.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));

        Exception exception = assertThrows(Exception.class, () ->
                turno.validarFechaFutura(LocalDateTime.of(2026, 8, 10, 12, 0))
        );

        assertTrue(exception.getMessage().contains("futuros"));
    }

    @Test
    public void turnoEnElFuturoNoLanzaExcepcion() throws Exception {
        Turno turno = new Turno();
        turno.setFechaHora(LocalDateTime.of(2026, 8, 20, 10, 0));

        turno.validarFechaFutura(LocalDateTime.of(2026, 8, 10, 12, 0));
    }

    @Test
    public void turnoSinFechaLanzaExcepcion() {
        Turno turno = new Turno();

        assertThrows(Exception.class, () ->
                turno.validarFechaFutura(LocalDateTime.now())
        );
    }

    // ==========================================================
    // MASCOTA: FECHA DE NACIMIENTO NO FUTURA
    // ==========================================================

    @Test
    public void mascotaConFechaNacimientoFuturaLanzaExcepcion() {
        Mascota mascota = new Mascota();
        mascota.setFechaNacimiento(LocalDate.now().plusDays(1));

        Exception exception = assertThrows(Exception.class, mascota::validarFechaNacimiento);

        assertTrue(exception.getMessage().contains("posterior"));
    }

    @Test
    public void mascotaNacidaHoyEsValida() throws Exception {
        Mascota mascota = new Mascota();
        mascota.setFechaNacimiento(LocalDate.now());

        mascota.validarFechaNacimiento();
    }

    @Test
    public void mascotaConFechaPasadaEsValida() throws Exception {
        Mascota mascota = new Mascota();
        mascota.setFechaNacimiento(LocalDate.now().minusYears(2));

        mascota.validarFechaNacimiento();
    }

    @Test
    public void mascotaSinFechaNacimientoLanzaExcepcion() {
        Mascota mascota = new Mascota();

        assertThrows(Exception.class, mascota::validarFechaNacimiento);
    }

    // ==========================================================
    // TURNO CANCELADO: LIBERA EL HORARIO
    // ==========================================================

    @Test
    public void turnoCanceladoNoBloqueaElHorarioDelVeterinario() throws Exception {
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 60);

        // Turno existente CANCELADO en el mismo horario
        Turno existente = new Turno();
        existente.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        existente.setEstado(EstadoTurno.CANCELADO);
        ItemTurno item = new ItemTurno(consulta, existente);
        item.setTiempoAlMomento(60);
        existente.agregarItem(item);

        // Nuevo turno en el mismo horario
        Turno nuevo = new Turno();
        nuevo.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        nuevo.agregarItem(new ItemTurno(consulta, nuevo));

        nuevo.validarDisponibilidad(List.of(existente));
    }

    @Test
    public void turnoPendienteBloqueaElHorarioDelVeterinario() {
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 60);

        Turno existente = new Turno();
        existente.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        existente.agregarItem(new ItemTurno(consulta, existente));

        Turno nuevo = new Turno();
        nuevo.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        nuevo.agregarItem(new ItemTurno(consulta, nuevo));

        assertThrows(Exception.class, () ->
                nuevo.validarDisponibilidad(List.of(existente)));
    }

    // ==========================================================
    // MASCOTA: DISPONIBILIDAD EN EL HORARIO
    // ==========================================================

    @Test
    public void mascotaNoPuedeTenerDosTurnosEnElMismoHorarioConOtroVeterinario() {
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 60);

        Turno existente = new Turno();
        existente.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        existente.agregarItem(new ItemTurno(consulta, existente));

        Turno nuevo = new Turno();
        nuevo.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        nuevo.agregarItem(new ItemTurno(consulta, nuevo));

        Exception exception = assertThrows(Exception.class, () ->
                nuevo.validarDisponibilidadMascota(List.of(existente)));

        assertTrue(exception.getMessage().contains("La mascota"));
    }

    @Test
    public void mascotaPuedeTenerTurnosEnHorariosNoSuperpuestos() throws Exception {
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 60);

        Turno existente = new Turno();
        existente.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        existente.agregarItem(new ItemTurno(consulta, existente));

        Turno nuevo = new Turno();
        nuevo.setFechaHora(LocalDateTime.of(2026, 8, 10, 11, 30));
        nuevo.agregarItem(new ItemTurno(consulta, nuevo));

        nuevo.validarDisponibilidadMascota(List.of(existente));
    }

    @Test
    public void consultaDentroDeGuarderiaDeLaMismaMascotaBloqueaElHorario() {
        ServicioGuarderia guarderia = guarderia(18000, 1440);
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 30);

        // Guardería: la mascota queda ocupada del 10/08 09:00 al 11/08 09:00
        Turno turnoGuarderia = new Turno();
        turnoGuarderia.setFechaHora(LocalDateTime.of(2026, 8, 10, 9, 0));
        turnoGuarderia.agregarItem(new ItemGuarderia(
                guarderia,
                turnoGuarderia,
                LocalDateTime.of(2026, 8, 10, 9, 0),
                LocalDateTime.of(2026, 8, 11, 9, 0)
        ));

        // Consulta de la misma mascota a las 10:00 (con otro veterinario)
        Turno turnoConsulta = new Turno();
        turnoConsulta.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        turnoConsulta.agregarItem(new ItemTurno(consulta, turnoConsulta));

        assertThrows(Exception.class, () ->
                turnoConsulta.validarDisponibilidadMascota(List.of(turnoGuarderia)));
    }

    @Test
    public void turnoCanceladoDeLaMascotaNoBloqueaElHorario() throws Exception {
        ServicioConsulta consulta = new ServicioConsulta("Consulta", 6500, 60);

        Turno existente = new Turno();
        existente.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        existente.setEstado(EstadoTurno.CANCELADO);
        existente.agregarItem(new ItemTurno(consulta, existente));

        Turno nuevo = new Turno();
        nuevo.setFechaHora(LocalDateTime.of(2026, 8, 10, 10, 0));
        nuevo.agregarItem(new ItemTurno(consulta, nuevo));

        nuevo.validarDisponibilidadMascota(List.of(existente));
    }

    // ==========================================================
    // NOMBRES: FORMATO TÍTULO (primera letra de cada palabra en mayúscula)
    // ==========================================================

    @Test
    public void capitalizarNombreConviertePrimeraLetraDeCadaPalabra() {
        assertEquals("Juan Perez", TextoUtil.capitalizar("juan perez"));
    }

    @Test
    public void capitalizarNombreRespetaAcentos() {
        assertEquals("María Del Carmen", TextoUtil.capitalizar("maría del carmen"));
    }

    @Test
    public void capitalizarNombreLimpiaEspaciosSobrantes() {
        assertEquals("Pedro Gómez", TextoUtil.capitalizar("  pedro   gómez  "));
    }

    @Test
    public void capitalizarNombreConTextoVacioONuloNoCambia() {
        assertEquals("", TextoUtil.capitalizar(""));
        assertNull(TextoUtil.capitalizar(null));
    }

    @Test
    public void clienteCapitalizaNombreYApellido() {
        Cliente cliente = new Cliente("juan carlos", "perez garcia", "12345678", "1111111111");

        assertEquals("Juan Carlos", cliente.getNombre());
        assertEquals("Perez Garcia", cliente.getApellido());

        cliente.setNombre("laura");
        cliente.setApellido("martínez");
        assertEquals("Laura", cliente.getNombre());
        assertEquals("Martínez", cliente.getApellido());
    }

    @Test
    public void mascotaCapitalizaSuNombre() {
        Mascota mascota = new Mascota();
        mascota.setNombre("rocky");

        assertEquals("Rocky", mascota.getNombre());

        Mascota otra = new Mascota("pelusa", LocalDate.now().minusYears(1), Sexo.HEMBRA, null);
        assertEquals("Pelusa", otra.getNombre());
    }

    // ==========================================================
    // VACUNACIÓN: PERIODICIDAD
    // ==========================================================    @Test
    public void vacunaDentroDePeriodicidadNoSePuedeRepetir() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "Rabia", 12);
        LocalDate ultima = LocalDate.of(2026, 1, 10);

        // Nueva aplicación antes de cumplir los 12 meses
        assertTrue(tipo.estaDentroDePeriodicidad(ultima, LocalDate.of(2026, 9, 1)));
    }

    @Test
    public void vacunaFueraDePeriodicidadSePuedeRepetir() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "Rabia", 12);
        LocalDate ultima = LocalDate.of(2026, 1, 10);

        // Nueva aplicación luego de cumplir los 12 meses
        assertFalse(tipo.estaDentroDePeriodicidad(ultima, LocalDate.of(2027, 1, 15)));
    }

    @Test
    public void vacunaEnLaFechaJustaDePeriodicidadSePuedeRepetir() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "Rabia", 12);
        LocalDate ultima = LocalDate.of(2026, 1, 10);

        // Exactamente en la fecha límite: no está dentro de la ventana
        assertFalse(tipo.estaDentroDePeriodicidad(ultima, LocalDate.of(2027, 1, 10)));
    }

    @Test
    public void vacunaSinAntecedentesNoEstaDentroDePeriodicidad() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "Rabia", 12);

        assertFalse(tipo.estaDentroDePeriodicidad(null, LocalDate.of(2026, 9, 1)));
    }
}
