package com.atm.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaManager {

    private static final String DEFAULT_PU = "atmPU";
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory(getPersistenceUnitName());

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private static String getPersistenceUnitName() {
        String fromProperty = System.getProperty("atm.persistence.unit");
        return (fromProperty == null || fromProperty.isBlank()) ? DEFAULT_PU : fromProperty.trim();
    }
}
