package com.veterinaria.service;

import com.veterinaria.model.Servicio;
import com.veterinaria.model.ServicioVacunacion;
import com.veterinaria.model.TipoVacuna;
import com.veterinaria.repository.ServicioRepository;
import com.veterinaria.repository.TipoVacunaRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Coordina la persistencia y las reglas de negocio de los servicios
 * (consulta, vacunación, guardería y peluquería). Los controladores no
 * tocan el EntityManager directamente.
 */
public class ServicioService {

    private final Transaccion transaccion;
    private final ServicioRepository servicioRepository;
    private final TipoVacunaRepository tipoVacunaRepository;

    public ServicioService(EntityManager em) {
        this.transaccion = new Transaccion(em);
        this.servicioRepository = new ServicioRepository(em);
        this.tipoVacunaRepository = new TipoVacunaRepository(em);
    }

    public void registrarServicio(Servicio servicio) {
        servicio.validar();

        try {
            transaccion.ejecutar(() -> {
                if (servicio instanceof ServicioVacunacion vacunacion
                        && vacunacion.getTipoVacuna() != null) {
                    vacunacion.setTipoVacuna(
                            tipoVacunaRepository.actualizar(vacunacion.getTipoVacuna())
                    );
                }
                servicioRepository.guardar(servicio);
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al registrar el servicio: " + e.getMessage(), e);
        }
    }

    public Servicio actualizarServicio(Servicio servicio) {
        servicio.validar();

        try {
            return transaccion.ejecutarConResultado(() -> {
                if (servicio instanceof ServicioVacunacion vacunacion
                        && vacunacion.getTipoVacuna() != null) {
                    vacunacion.setTipoVacuna(
                            tipoVacunaRepository.actualizar(vacunacion.getTipoVacuna())
                    );
                }
                return servicioRepository.actualizar(servicio);
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al actualizar el servicio: " + e.getMessage(), e);
        }
    }

    public void eliminarServicio(Long idServicio) {
        if (idServicio == null || servicioRepository.tieneUso(idServicio)) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un servicio que ya fue utilizado en un turno."
            );
        }

        try {
            transaccion.ejecutar(() ->
                    servicioRepository.buscarPorId(idServicio)
                            .ifPresent(servicioRepository::eliminar));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al eliminar el servicio: " + e.getMessage(), e);
        }
    }

    public List<Servicio> listarServicios() {
        return servicioRepository.buscarTodos();
    }

    public List<TipoVacuna> listarTiposVacuna() {
        return tipoVacunaRepository.buscarTodos();
    }
}
