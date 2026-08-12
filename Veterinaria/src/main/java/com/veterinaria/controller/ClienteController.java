package com.veterinaria.controller;

import com.veterinaria.model.Cliente;
import com.veterinaria.model.Mascota;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.MascotaRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ClienteController {

    private final EntityManager em;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;

    public ClienteController(EntityManager em) {
        this.em = em;
        this.clienteRepository = new ClienteRepository(em);
        this.mascotaRepository = new MascotaRepository(em);
    }

    public void registrarCliente(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }
        if (cliente.getDni() == 0 || cliente.getDni() <= 0 || String.valueOf(cliente.getDni()).length() != 8) {
            throw new IllegalArgumentException("El DNI es obligatorio.");
        }

        if (cliente.getTelefono() == 0 || cliente.getTelefono() <= 0 || String.valueOf(cliente.getTelefono()).length() < 8) {
            throw new IllegalArgumentException("El teléfono es obligatorio y debe tener al menos 8 dígitos.");
        }

        try {
            em.getTransaction().begin();
            clienteRepository.guardar(cliente);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public void eliminar(Long id) {
    em.getTransaction().begin();
    try {
        clienteRepository.buscarPorId(id).ifPresent(cliente -> {
            clienteRepository.eliminar(cliente);
        });
        em.getTransaction().commit();
    } catch (Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        throw new RuntimeException("Error al eliminar el cliente: " + e.getMessage(), e);
    }
    }

    public Cliente actualizar(Cliente cliente) {
    em.getTransaction().begin();
    try {
        Cliente clienteActualizado = clienteRepository.actualizar(cliente);
        em.getTransaction().commit();
        return clienteActualizado;
    } catch (Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        throw new RuntimeException("Error al actualizar el cliente: " + e.getMessage(), e);
    }
}

    public void agregarMascotaACliente(long idCliente, Mascota mascota) {
        mascota.validarFechaNacimiento();

        try {
            em.getTransaction().begin();

            Cliente cliente = clienteRepository.buscarPorId(idCliente)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el cliente con ID: " + idCliente));

            // Guardamos la mascota primero
            mascotaRepository.guardar(mascota);
            
            // La agregamos a la lista del cliente (relación unidireccional)
            cliente.getMascotas().add(mascota);
            clienteRepository.actualizar(cliente);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.buscarTodos();
    }
}