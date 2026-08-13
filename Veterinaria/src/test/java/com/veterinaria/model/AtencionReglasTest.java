package com.veterinaria.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

/**
 * Reglas de validación de los detalles de atención (regla revisada: un
 * detalle cargado no puede quedar vacío; cada tipo exige su contenido mínimo).
 */
public class AtencionReglasTest {

    // ==========================================================
    // DETALLE ATENCIÓN BASE (guardería / peluquería)
    // ==========================================================

    @Test
    public void detalleAtencionExigeObservaciones() {
        DetalleAtencion detalle = new DetalleAtencion("");

        Exception exception = assertThrows(IllegalArgumentException.class,
                detalle::validar);

        assertTrue(exception.getMessage().contains("no puede estar vacío"));
    }

    @Test
    public void detalleAtencionSinObservacionesNulasRechazado() {
        DetalleAtencion detalle = new DetalleAtencion(null);

        assertThrows(IllegalArgumentException.class, detalle::validar);
    }

    @Test
    public void detalleAtencionConObservacionesEsValido() {
        DetalleAtencion detalle =
                new DetalleAtencion("Peluquería completa con corte sanitario");

        detalle.validar();
    }

    // ==========================================================
    // DETALLE CONSULTA
    // ==========================================================

    @Test
    public void detalleConsultaExigeDiagnostico() {
        DetalleConsulta detalle = new DetalleConsulta("", "");

        Exception exception = assertThrows(IllegalArgumentException.class,
                detalle::validar);

        assertTrue(exception.getMessage().contains("diagnóstico"));
    }

    @Test
    public void detalleConsultaConDiagnosticoEnBlancoRechazado() {
        DetalleConsulta detalle = new DetalleConsulta("paciente tranquilo", "   ");

        assertThrows(IllegalArgumentException.class, detalle::validar);
    }

    @Test
    public void detalleConsultaConDiagnosticoEsValido() {
        DetalleConsulta detalle =
                new DetalleConsulta("Paciente tranquilo", "Otitis externa");

        detalle.validar();
    }

    @Test
    public void detalleConsultaValidoNoExigeObservaciones() {
        DetalleConsulta detalle = new DetalleConsulta(null, "Gingivitis leve");

        detalle.validar();
    }

    // ==========================================================
    // DETALLE VACUNACIÓN
    // ==========================================================

    @Test
    public void detalleVacunacionExigeLaboratorioODosis() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "Rabia", 12, true);
        DetalleVacunacion detalle = new DetalleVacunacion(tipo, null, null);

        Exception exception = assertThrows(IllegalArgumentException.class,
                detalle::validar);

        assertTrue(exception.getMessage().contains("laboratorio/marca"));
    }

    @Test
    public void detalleVacunacionConLaboratorioEsValido() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "Rabia", 12, true);
        DetalleVacunacion detalle =
                new DetalleVacunacion(tipo, "Zoetis", null);

        detalle.validar();
    }

    @Test
    public void detalleVacunacionConDosisEsValido() {
        TipoVacuna tipo = new TipoVacuna("Quíntuple", "Moquillo", 12, true);
        DetalleVacunacion detalle =
                new DetalleVacunacion(tipo, null, "1ra dosis - refuerzo a los 21 días");

        detalle.validar();
    }

    // ==========================================================
    // TRATAMIENTO
    // ==========================================================

    @Test
    public void tratamientoExigeDescripcion() {
        Tratamiento tratamiento = new Tratamiento(
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20),
                ""
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                tratamiento::validar);

        assertTrue(exception.getMessage().contains("descripción"));
    }

    @Test
    public void tratamientoExigeFechas() {
        Tratamiento tratamiento = new Tratamiento(null, null, "Antibiótico");

        Exception exception = assertThrows(IllegalArgumentException.class,
                tratamiento::validar);

        assertTrue(exception.getMessage().contains("fecha"));
    }

    @Test
    public void tratamientoConFechaFinAnteriorAInicioRechazado() {
        Tratamiento tratamiento = new Tratamiento(
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 10),
                "Antibiótico"
        );

        Exception exception = assertThrows(IllegalArgumentException.class,
                tratamiento::validar);

        assertTrue(exception.getMessage().contains("no puede ser anterior"));
    }

    @Test
    public void tratamientoConRangoValidoEsAceptado() {
        Tratamiento tratamiento = new Tratamiento(
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20),
                "Antiinflamatorio, 5 días"
        );

        tratamiento.validar();
    }

    @Test
    public void tratamientoConMismaFechaInicioYFinEsValido() {
        Tratamiento tratamiento = new Tratamiento(
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 10),
                "Dosis única"
        );

        tratamiento.validar();
    }
}
