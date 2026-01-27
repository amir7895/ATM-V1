package com.atm.db;

import com.atm.model.Account;
import com.atm.model.ATMState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class DataInitializer {

    public static void initializeTestData() {
        EntityManager em = JpaManager.getEntityManager();
        
        try {
            // Check if data already exists
            TypedQuery<Long> countQuery = em.createQuery("SELECT COUNT(a) FROM Account a", Long.class);
            Long count = countQuery.getSingleResult();
            
            if (count > 0) {
                System.out.println("Test data already exists, skipping initialization.");
                return;
            }
            
            em.getTransaction().begin();
            
            // Create test accounts
            Account acc1 = new Account();
            acc1.setAccountId("ACC001");
            acc1.setCardNumber("1111");
            acc1.setPin("1111");
            acc1.setBalance(5000.0);
            acc1.setFailedAttempts(0);
            
            Account acc2 = new Account();
            acc2.setAccountId("ACC002");
            acc2.setCardNumber("2222");
            acc2.setPin("2222");
            acc2.setBalance(3000.0);
            acc2.setFailedAttempts(0);
            
            // Create ATM State
            ATMState atmState = new ATMState();
            atmState.setCash(10000.0);
            atmState.setPaper(20);
            atmState.setInk(20);
            atmState.setFirmwareVersion("v1.0");
            
            em.persist(acc1);
            em.persist(acc2);
            em.persist(atmState);
            
            em.getTransaction().commit();
            System.out.println("\n=== Test data initialized successfully! ===");
            System.out.println("Customer 1 - Card: 1111, PIN: 1111 (Balance: 5000)");
            System.out.println("Customer 2 - Card: 2222, PIN: 2222 (Balance: 3000)");
            System.out.println("ATM State - Cash: 10000, Paper: 20, Ink: 20, Firmware: v1.0\n");
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Error initializing test data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}

