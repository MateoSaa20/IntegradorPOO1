package com.veterinaria.service;

import jakarta.persistence.EntityManager;

/**
 * Coordina las transacciones de persistencia entre el controlador y JPA.
 * Encapsula begin/commit/rollback para que la lógica de transacciones no
 * se repita en cada operación y quede fuera de los controladores.
 */
public final class Transaccion {

    private final EntityManager em;

    public Transaccion(EntityManager em) {
        this.em = em;
    }

    /**
     * Ejecuta un bloque de trabajo dentro de una transacción. Si el trabajo
     * lanza una excepción, se revierte y se re-lanza.
     */
    public void ejecutar(Trabajo trabajo) throws Exception {
        try {
            em.getTransaction().begin();
            trabajo.ejecutar();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    /**
     * Ejecuta un bloque de trabajo que devuelve un resultado, dentro de una
     * transacción. Si el trabajo lanza una excepción, se revierte y se
     * re-lanza.
     */
    public <T> T ejecutarConResultado(TrabajoConResultado<T> trabajo) throws Exception {
        try {
            em.getTransaction().begin();
            T resultado = trabajo.ejecutar();
            em.getTransaction().commit();
            return resultado;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    @FunctionalInterface
    public interface Trabajo {
        void ejecutar() throws Exception;
    }

    @FunctionalInterface
    public interface TrabajoConResultado<T> {
        T ejecutar() throws Exception;
    }
}
