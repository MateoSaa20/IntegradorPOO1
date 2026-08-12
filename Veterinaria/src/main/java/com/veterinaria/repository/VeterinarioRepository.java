package com.veterinaria.repository;

import com.veterinaria.model.Veterinario;
import jakarta.persistence.EntityManager;
import java.util.Optional;

public class VeterinarioRepository extends BaseRepository<Veterinario, Long> {

    public VeterinarioRepository(EntityManager em) {
        super(em, Veterinario.class);
    }

    public Optional<Veterinario> buscarPorMatricula(String matricula) {
        return em.createQuery("SELECT v FROM Veterinario v WHERE v.matricula = :matricula", Veterinario.class)
                 .setParameter("matricula", matricula)
                 .getResultStream()
                 .findFirst();
    }

    /**
     * Regla de negocio: un veterinario que tiene turnos asignados no puede
     * eliminarse porque los turnos conservan su referencia histórica.
     */
    public boolean tieneTurnos(Long idVeterinario) {
        Long cantidad = em.createQuery(
                "SELECT COUNT(t) FROM Turno t WHERE t.veterinario.id = :idVeterinario",
                Long.class)
                .setParameter("idVeterinario", idVeterinario)
                .getSingleResult();

        return cantidad != null && cantidad > 0;
    }
}