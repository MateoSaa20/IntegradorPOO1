package com.veterinaria.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas de negocio de los ABM: servicios, tipos de vacuna y veterinarios.
 */
public class ABMReglasTest {

    // ==========================================================
    // SERVICIO: VALIDACIÓN DE CAMPOS COMUNES
    // ==========================================================

    @Test
    public void servicioSinNombreLanzaExcepcion() {
        ServicioConsulta servicio = new ServicioConsulta("", 6500, 30);

        assertThrows(IllegalArgumentException.class, servicio::validar);
    }

    @Test
    public void servicioConPrecioCeroLanzaExcepcion() {
        ServicioConsulta servicio = new ServicioConsulta("Consulta", 0, 30);

        assertThrows(IllegalArgumentException.class, servicio::validar);
    }

    @Test
    public void servicioConDuracionInvalidaLanzaExcepcion() {
        ServicioConsulta servicio = new ServicioConsulta("Consulta", 6500, 0);

        assertThrows(IllegalArgumentException.class, servicio::validar);
    }

    @Test
    public void servicioValidoNormalizaSuNombre() {
        ServicioConsulta servicio = new ServicioConsulta("consulta clinica", 6500, 30);

        servicio.validar();

        assertEquals("Consulta Clinica", servicio.getNombre());
    }

    @Test
    public void guarderiaValidaConSusCamposComunes() {
        ServicioGuarderia guarderia = new ServicioGuarderia("guardería canina", 18000, 1440);

        guarderia.validar();

        assertEquals("Guardería Canina", guarderia.getNombre());
    }

    @Test
    public void peluqueriaValidaConSusCamposComunes() {
        ServicioPeluqueria peluqueria = new ServicioPeluqueria("peluquería y baño", 14000, 60);

        peluqueria.validar();

        assertEquals("Peluquería Y Baño", peluqueria.getNombre());
    }

    // ==========================================================
    // SERVICIO DE VACUNACIÓN: TIPO DE VACUNA OBLIGATORIO
    // ==========================================================

    @Test
    public void vacunacionSinTipoDeVacunaLanzaExcepcion() {
        ServicioVacunacion servicio = new ServicioVacunacion("Vacuna Antirrábica", 9000, 15, null);

        assertThrows(IllegalArgumentException.class, servicio::validar);
    }

    @Test
    public void vacunacionConTipoDeVacunaEsValida() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "Rabia", 12);
        ServicioVacunacion servicio = new ServicioVacunacion("Vacuna Antirrábica", 9000, 15, tipo);

        servicio.validar();
    }

    // ==========================================================
    // TIPO DE VACUNA: VALIDACIÓN DE CAMPOS
    // ==========================================================

    @Test
    public void tipoVacunaSinNombreLanzaExcepcion() {
        TipoVacuna tipo = new TipoVacuna("", "Rabia", 12);

        assertThrows(IllegalArgumentException.class, tipo::validar);
    }

    @Test
    public void tipoVacunaSinEnfermedadLanzaExcepcion() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "", 12);

        assertThrows(IllegalArgumentException.class, tipo::validar);
    }

    @Test
    public void tipoVacunaConPeriodicidadInvalidaLanzaExcepcion() {
        TipoVacuna tipo = new TipoVacuna("Rabisin", "Rabia", 0);

        assertThrows(IllegalArgumentException.class, tipo::validar);
    }

    @Test
    public void tipoVacunaValidoNormalizaSuNombre() {
        TipoVacuna tipo = new TipoVacuna("rabisin", "rabia", 12);

        tipo.validar();

        assertEquals("Rabisin", tipo.getNombreComercial());
        assertEquals("Rabia", tipo.getEnfermedadQuePreviene());
    }

    // ==========================================================
    // VETERINARIO: VALIDACIÓN DE CAMPOS
    // ==========================================================

    @Test
    public void veterinarioSinNombreLanzaExcepcion() {
        Veterinario vet = new Veterinario("", "Gómez", "MP-1042");

        assertThrows(IllegalArgumentException.class, vet::validar);
    }

    @Test
    public void veterinarioSinApellidoLanzaExcepcion() {
        Veterinario vet = new Veterinario("Carlos", "", "MP-1042");

        assertThrows(IllegalArgumentException.class, vet::validar);
    }

    @Test
    public void veterinarioSinMatriculaLanzaExcepcion() {
        Veterinario vet = new Veterinario("Carlos", "Gómez", "");

        assertThrows(IllegalArgumentException.class, vet::validar);
    }

    @Test
    public void veterinarioValidoNormalizaNombreYApellido() {
        Veterinario vet = new Veterinario("carlos", "gómez", "MP-1042");

        vet.validar();

        assertEquals("Carlos", vet.getNombre());
        assertEquals("Gómez", vet.getApellido());
    }
}
