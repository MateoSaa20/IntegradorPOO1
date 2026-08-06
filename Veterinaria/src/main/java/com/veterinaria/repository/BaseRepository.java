package com.veterinaria.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class BaseRepository<T, ID> {

    protected final EntityManager em;
    private final Class<T> entityClass;

    public BaseRepository(EntityManager em, Class<T> entityClass) {
        this.em = em;
        this.entityClass = entityClass;
    }

    public T guardar(T entidad) {
        em.persist(entidad);
        return entidad;
    }

    public T actualizar(T entidad) {
        return em.merge(entidad);
    }

    public Optional<T> buscarPorId(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public List<T> buscarTodos() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return em.createQuery(jpql, entityClass).getResultList();
    }

    public void eliminar(T entidad) {
        em.remove(em.contains(entidad) ? entidad : em.merge(entidad));
    }
}