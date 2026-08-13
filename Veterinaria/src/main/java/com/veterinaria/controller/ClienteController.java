package com.veterinaria.controller;

import com.veterinaria.model.Cliente;
import com.veterinaria.model.Especie;
import com.veterinaria.model.Mascota;
import com.veterinaria.model.Raza;
import com.veterinaria.service.ClienteService;

import java.util.List;

/**
 * Expone las operaciones de clientes y mascotas a la capa de vista,
 * delegando la lógica y la persistencia a la capa de servicios.
 */
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public void registrarCliente(Cliente cliente) {
        clienteService.registrarCliente(cliente);
    }

    public void eliminar(Long id) {
        clienteService.eliminarCliente(id);
    }

    public Cliente actualizar(Cliente cliente) {
        return clienteService.actualizarCliente(cliente);
    }

    public void agregarMascotaACliente(long idCliente, Mascota mascota) {
        clienteService.agregarMascotaACliente(idCliente, mascota);
    }

    public void eliminarMascota(Mascota mascota) {
        clienteService.eliminarMascota(mascota);
    }

    public void actualizarMascota(Mascota mascota) {
        clienteService.actualizarMascota(mascota);
    }

    public List<Cliente> listarTodos() {
        return clienteService.listarClientes();
    }

    public List<Especie> listarEspecies() {
        return clienteService.listarEspecies();
    }

    public List<Raza> listarRazasPorEspecie(Especie especie) {
        return clienteService.listarRazasPorEspecie(especie);
    }
}
