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

    public EntityManager getEntityManager() {
        return em;
    }

    public T guardar(T entidad) {
        em.persist(entidad);
        return entidad;
    }

    public T actualizar(T entidad) {
        return em.merge(entidad);
    }

    public Optional<T> buscarPorId(ID id) {
        T entity = em.find(entityClass, id);
        // ofNullable envuelve la entidad si existe, o devuelve un Optional vacío si es null
        return Optional.ofNullable(entity);
    }

    public List<T> buscarTodos() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return em.createQuery(jpql, entityClass).getResultList();
    }

    public void eliminar(T entidad) {
        em.remove(em.contains(entidad) ? entidad : em.merge(entidad));
    }
}