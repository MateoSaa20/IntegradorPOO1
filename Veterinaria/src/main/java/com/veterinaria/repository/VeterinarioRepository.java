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
}