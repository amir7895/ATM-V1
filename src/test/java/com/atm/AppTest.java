package com.atm;

import com.atm.db.JpaManager;
import com.atm.model.Account;
import com.atm.model.ATMState;
import com.atm.service.ATMService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {

    private ATMService service;

    @BeforeEach
    public void setup() {
        service = new ATMService();
        resetTestData();
    }

    @Test
    public void customerLoginTest() {
        Account account = service.login("1111", "1111");
        assertNotNull(account);
        assertEquals("1111", account.getCardNumber());
    }

    @Test
    public void withdrawTestBalanceDecreases() {
        Account account = service.login("1111", "1111");
        boolean success = service.withdraw(account, 200.0);

        assertTrue(success);
        Account refreshed = service.getAccountDetails(account.getAccountId());
        assertEquals(4800.0, refreshed.getBalance(), 0.01);
    }

    @Test
    public void depositTestBalanceIncreases() {
        Account account = service.login("1111", "1111");
        boolean success = service.deposit(account, 250.0);

        assertTrue(success);
        Account refreshed = service.getAccountDetails(account.getAccountId());
        assertEquals(5250.0, refreshed.getBalance(), 0.01);
    }

    @Test
    public void transferTestBalancesChange() {
        Account sender = service.login("1111", "1111");
        boolean success = service.transfer(sender, "2222", 500.0);

        assertTrue(success);

        Account refreshedSender = service.getAccountDetails(sender.getAccountId());
        Account receiver = findAccountByCard("2222");

        assertEquals(4500.0, refreshedSender.getBalance(), 0.01);
        assertEquals(3500.0, receiver.getBalance(), 0.01);
    }

    @Test
    public void balanceViewTest() {
        Account account = service.login("1111", "1111");
        Account refreshed = service.getAccountDetails(account.getAccountId());
        assertEquals(5000.0, refreshed.getBalance(), 0.01);
        assertEquals(5000.0, service.getBalance(refreshed), 0.01);
    }

    @Test
    public void technicianViewOnlyATMStatusTest() {
        ATMState before = getATMState();

        String output = captureOutput(service::viewATMStatus);

        ATMState after = getATMState();

        assertTrue(output.contains("ATM STATUS"));
        assertEquals(before.getCash(), after.getCash(), 0.01);
        assertEquals(before.getPaper(), after.getPaper());
        assertEquals(before.getInk(), after.getInk());
    }

    private void resetTestData() {
        EntityManager em = JpaManager.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Transaction").executeUpdate();
            em.createQuery("DELETE FROM Account").executeUpdate();
            em.createQuery("DELETE FROM ATMState").executeUpdate();
            em.getTransaction().commit();

            em.getTransaction().begin();

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

            ATMState atmState = new ATMState();
            atmState.setCash(10000.0);
            atmState.setPaper(20);
            atmState.setInk(20);
            atmState.setFirmwareVersion("v1.0");

            em.persist(acc1);
            em.persist(acc2);
            em.persist(atmState);

            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private Account findAccountByCard(String cardNumber) {
        EntityManager em = JpaManager.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Account a WHERE a.cardNumber = :card",
                    Account.class
                )
                .setParameter("card", cardNumber)
                .getSingleResult();
        } finally {
            em.close();
        }
    }

    private ATMState getATMState() {
        EntityManager em = JpaManager.getEntityManager();
        try {
            return em.createQuery("SELECT a FROM ATMState a", ATMState.class)
                .getSingleResult();
        } finally {
            em.close();
        }
    }

    private String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            action.run();
            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalOut);
        }
    }
}
