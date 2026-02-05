package com.atm.service;

import com.atm.db.JpaManager;
import com.atm.model.Account;
import com.atm.model.ATMState;
import com.atm.model.Transaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ATMService {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /* ================= LOGIN ================= */

    public Account login(String cardNumber, String pin) {
        EntityManager em = JpaManager.getEntityManager();
        try {
            TypedQuery<Account> q = em.createQuery(
                "SELECT a FROM Account a WHERE a.cardNumber = :card AND a.pin = :pin",
                Account.class
            );
            q.setParameter("card", cardNumber);
            q.setParameter("pin", pin);

            Account account = q.getSingleResult();
            account.setFailedAttempts(0); // Reset failed attempts on successful login
            
            em.getTransaction().begin();
            em.merge(account);
            em.getTransaction().commit();
            
            return account;
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /* ================= ACCOUNT OPERATIONS ================= */

    public Account getAccountDetails(String accountId) {
        EntityManager em = JpaManager.getEntityManager();
        try {
            return em.find(Account.class, accountId);
        } finally {
            em.close();
        }
    }

    public double getBalance(Account account) {
        return account.getBalance();
    }

    /* ================= DEPOSIT ================= */

    public boolean deposit(Account account, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid amount. Please enter a positive number.");
            return false;
        }

        EntityManager em = JpaManager.getEntityManager();
        em.getTransaction().begin();

        try {
            Account managed = em.find(Account.class, account.getAccountId());
            ATMState atmState = getATMState(em);

            managed.setBalance(managed.getBalance() + amount);
            atmState.addCash(amount);

            Transaction t = new Transaction();
            t.setAccount(managed);
            t.setAmount(amount);
            t.setType("DEPOSIT");
            t.setTime(LocalDateTime.now());
            t.setCreatedAt(LocalDateTime.now());

            em.persist(t);
            em.merge(atmState);
            em.getTransaction().commit();

            System.out.println("Deposit successful.");
            account.setBalance(managed.getBalance());
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Deposit failed: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /* ================= WITHDRAW ================= */

    public boolean withdraw(Account account, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid amount. Please enter a positive number.");
            return false;
        }

        EntityManager em = JpaManager.getEntityManager();
        em.getTransaction().begin();

        try {
            Account managed = em.find(Account.class, account.getAccountId());
            ATMState atmState = getATMState(em);

            // Validate
            if (managed.getBalance() < amount) {
                System.out.println("Insufficient balance.");
                em.getTransaction().rollback();
                return false;
            }

            if (atmState.getCash() < amount) {
                System.out.println("ATM has insufficient cash.");
                em.getTransaction().rollback();
                return false;
            }

            if (atmState.getPaper() < 1) {
                System.out.println("ATM is out of paper.");
                em.getTransaction().rollback();
                return false;
            }

            if (atmState.getInk() < 1) {
                System.out.println("ATM is out of ink.");
                em.getTransaction().rollback();
                return false;
            }

            // Process withdrawal
            managed.setBalance(managed.getBalance() - amount);
            atmState.setCash(atmState.getCash() - amount);

            Transaction t = new Transaction();
            t.setAccount(managed);
            t.setAmount(amount);
            t.setType("WITHDRAW");
            t.setTime(LocalDateTime.now());
            t.setCreatedAt(LocalDateTime.now());

            em.persist(t);
            em.merge(atmState);
            em.getTransaction().commit();

            System.out.println("Withdrawal successful.");
            account.setBalance(managed.getBalance());
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Withdrawal failed: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /* ================= TRANSFER ================= */

    public boolean transfer(Account from, String toCardNumber, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid amount. Please enter a positive number.");
            return false;
        }

        EntityManager em = JpaManager.getEntityManager();
        em.getTransaction().begin();

        try {
            Account sender = em.find(Account.class, from.getAccountId());

            TypedQuery<Account> q = em.createQuery(
                "SELECT a FROM Account a WHERE a.cardNumber = :card",
                Account.class
            );
            q.setParameter("card", toCardNumber);

            Account receiver;
            try {
                receiver = q.getSingleResult();
            } catch (NoResultException e) {
                System.out.println("Target account not found.");
                em.getTransaction().rollback();
                return false;
            }

            if (sender.getBalance() < amount) {
                System.out.println("Insufficient balance.");
                em.getTransaction().rollback();
                return false;
            }

            // Process transfer
            sender.setBalance(sender.getBalance() - amount);
            receiver.setBalance(receiver.getBalance() + amount);

            Transaction t1 = new Transaction();
            t1.setAccount(sender);
            t1.setAmount(amount);
            t1.setType("TRANSFER_OUT");
            t1.setCreatedAt(LocalDateTime.now());

            Transaction t2 = new Transaction();
            t2.setAccount(receiver);
            t2.setAmount(amount);
            t2.setType("TRANSFER_IN");
            t2.setTime(LocalDateTime.now());
            t2.setCreatedAt(LocalDateTime.now());

            em.persist(t1);
            em.persist(t2);
            em.getTransaction().commit();

            System.out.println("Transfer successful.");
            from.setBalance(sender.getBalance());
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Transfer failed: " + e.getMessage());
            return false;
        } finally {
            em.close();
        }
    }

    /* ================= RECEIPT ================= */

    public void printReceipt(String type, double amount, double balance) {
        EntityManager em = JpaManager.getEntityManager();
        em.getTransaction().begin();

        try {
            ATMState state = getATMState(em);
            
            // Decrement paper and ink when printing receipt
            if (state.getPaper() > 0) {
                state.setPaper(state.getPaper() - 1);
            } else {
                System.out.println("Warning: Out of paper!");
            }
            
            if (state.getInk() > 0) {
                state.setInk(state.getInk() - 1);
            } else {
                System.out.println("Warning: Out of ink!");
            }
            
            em.merge(state);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            System.out.println("Error updating receipt supplies: " + e.getMessage());
        } finally {
            em.close();
        }

        System.out.println("\n--------- RECEIPT ---------");
        System.out.printf("Type   : %s\n", type);
        System.out.printf("Amount : %.2f\n", amount);
        System.out.printf("Balance: %.2f\n", balance);
        System.out.printf("Date   : %s\n", LocalDateTime.now().format(dateFormatter));
        System.out.println("---------------------------\n");
    }

    /* ================= TECHNICIAN OPERATIONS ================= */

    public void viewATMStatus() {
        EntityManager em = JpaManager.getEntityManager();
        try {
            ATMState state = getATMState(em);
            System.out.println("\n===== ATM STATUS =====");
            System.out.printf("Cash: $%.2f\n", state.getCash());
            System.out.printf("Paper: %d\n", state.getPaper());
            System.out.printf("Ink: %d\n", state.getInk());
            System.out.println("====================\n");
        } finally {
            em.close();
        }
    }

    public void refillPaper(int amount) {
        System.out.println("Technician actions are read-only in V1.");
    }

    public void refillInk(int amount) {
        System.out.println("Technician actions are read-only in V1.");
    }

    public void addCashToATM(double amount) {
        System.out.println("Technician actions are read-only in V1.");
    }

    public void collectAllCash() {
        System.out.println("Technician actions are read-only in V1.");
    }

    public void updateFirmware(String version) {
        System.out.println("Technician actions are read-only in V1.");
    }

    /* ================= HELPER METHODS ================= */

    private ATMState getATMState(EntityManager em) {
        TypedQuery<ATMState> q = em.createQuery("SELECT a FROM ATMState a", ATMState.class);
        return q.getSingleResult();
    }
}
