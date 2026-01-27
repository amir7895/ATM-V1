package com.atm.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaManager {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("atmPU");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
