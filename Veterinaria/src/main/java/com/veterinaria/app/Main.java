package com.veterinaria.app;

import com.veterinaria.config.JpaUtil;
import jakarta.persistence.EntityManager;

public class Main {

    public static void main(String[] args) {

        EntityManager em = JpaUtil.getEntityManager();

        System.out.println("Conexión a la base de datos realizada correctamente.");

        em.close();

        JpaUtil.close();

    }
}