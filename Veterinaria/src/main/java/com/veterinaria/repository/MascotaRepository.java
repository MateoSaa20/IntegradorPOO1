package com.veterinaria.repository;

import com.veterinaria.model.Mascota;
import jakarta.persistence.EntityManager;
import java.util.List;

public class MascotaRepository extends BaseRepository<Mascota, Long> {

    public MascotaRepository(EntityManager em) {
        super(em, Mascota.class);
    }

    // Consulta navegando desde Cliente hacia la colección de mascotas
    public List<Mascota> buscarPorClienteId(long idCliente) {
        String jpql = "SELECT m FROM Cliente c JOIN c.mascotas m WHERE c.idCliente = :idCliente";
        return em.createQuery(jpql, Mascota.class)
                 .setParameter("idCliente", idCliente)
                 .getResultList();
    }
}