package com.veterinaria.repository;

import com.veterinaria.model.Cliente;
import jakarta.persistence.EntityManager;
import java.util.Optional;

public class ClienteRepository extends BaseRepository<Cliente, Long> {

    public ClienteRepository(EntityManager em) {
        super(em, Cliente.class);
    }

    public Optional<Cliente> buscarPorDni(String dni) {
        return em.createQuery(
                        "SELECT c FROM Cliente c " +
                        "WHERE REPLACE(c.dni, '.', '') = :dni",
                        Cliente.class)
                 .setParameter("dni", dni)
                 .getResultStream()
                 .findFirst();
    }
}
