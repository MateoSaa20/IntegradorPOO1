package com.veterinaria.service;

import com.veterinaria.model.Cliente;
import com.veterinaria.model.Especie;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.Raza;
import com.veterinaria.repository.ClienteRepository;
import com.veterinaria.repository.EspecieRepository;
import com.veterinaria.repository.MascotaRepository;
import com.veterinaria.repository.RazaRepository;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Coordina la persistencia y las reglas de negocio de clientes y mascotas.
 * Es la capa entre los controladores y JPA: los controladores no tocan el
 * EntityManager directamente.
 */
public class ClienteService {

    private final Transaccion transaccion;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;
    private final EspecieRepository especieRepository;
    private final RazaRepository razaRepository;

    public ClienteService(EntityManager em) {
        this.transaccion = new Transaccion(em);
        this.clienteRepository = new ClienteRepository(em);
        this.mascotaRepository = new MascotaRepository(em);
        this.especieRepository = new EspecieRepository(em);
        this.razaRepository = new RazaRepository(em);
    }

    public void registrarCliente(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }
        if (cliente.getDni() == null || cliente.getDni().isBlank()
                || !cliente.getDni().matches("\\d+") || cliente.getDni().length() > 8) {
            throw new IllegalArgumentException("El DNI es obligatorio y debe tener hasta 8 dígitos.");
        }
        if (cliente.getTelefono() == null || cliente.getTelefono().isBlank()
                || !cliente.getTelefono().matches("\\d+") || cliente.getTelefono().length() > 12) {
            throw new IllegalArgumentException("El teléfono es obligatorio y debe tener hasta 12 dígitos.");
        }

        try {
            transaccion.ejecutar(() -> clienteRepository.guardar(cliente));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al registrar el cliente: " + e.getMessage(), e);
        }
    }

    public Cliente actualizarCliente(Cliente cliente) {
        try {
            return transaccion.ejecutarConResultado(() -> clienteRepository.actualizar(cliente));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al actualizar el cliente: " + e.getMessage(), e);
        }
    }

    public void eliminarCliente(Long id) {
        try {
            transaccion.ejecutar(() ->
                    clienteRepository.buscarPorId(id).ifPresent(clienteRepository::eliminar));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al eliminar el cliente: " + e.getMessage(), e);
        }
    }

    public void agregarMascotaACliente(long idCliente, Mascota mascota) {
        mascota.validarFechaNacimiento();

        try {
            transaccion.ejecutar(() -> {
                Cliente cliente = clienteRepository.buscarPorId(idCliente)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No se encontró el cliente con ID: " + idCliente));

                mascotaRepository.guardar(mascota);
                cliente.getMascotas().add(mascota);
                clienteRepository.actualizar(cliente);
            });
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al agregar la mascota: " + e.getMessage(), e);
        }
    }

    public void eliminarMascota(Mascota mascota) {
        try {
            transaccion.ejecutar(() -> mascotaRepository.eliminar(mascota));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al eliminar la mascota: " + e.getMessage(), e);
        }
    }

    public void actualizarMascota(Mascota mascota) {
        try {
            transaccion.ejecutar(() -> mascotaRepository.actualizar(mascota));
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Error al actualizar la mascota: " + e.getMessage(), e);
        }
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.buscarTodos();
    }

    public Optional<Cliente> buscarPorDni(String dni) {
        if (dni == null) {
            return Optional.empty();
        }
        String normalizado = dni.trim().replace(".", "");
        if (normalizado.isEmpty()) {
            return Optional.empty();
        }
        return clienteRepository.buscarPorDni(normalizado);
    }

    public List<Mascota> listarMascotasDe(Cliente cliente) {
        if (cliente == null || cliente.getIdCliente() == null) {
            return List.of();
        }
        Cliente clienteCompleto = clienteRepository.buscarPorId(cliente.getIdCliente())
                .orElse(null);
        return clienteCompleto != null ? clienteCompleto.getMascotas() : List.of();
    }

    public List<Especie> listarEspecies() {
        return especieRepository.buscarTodos();
    }

    public List<Raza> listarRazasPorEspecie(Especie especie) {
        return razaRepository.buscarPorEspecie(especie);
    }
}
