package com.atm.ui;

import com.atm.model.Account;
import com.atm.service.ATMService;

import java.util.Scanner;

public class ATMConsoleUI {

    private final ATMService service;
    private final Scanner sc = new Scanner(System.in);

    public ATMConsoleUI(ATMService service) {
        this.service = service;
    }

    public void start() {
        while (true) {
            System.out.println("\n===== ATM SYSTEM =====");
            System.out.println("1. Customer");
            System.out.println("2. Technician");
            System.out.println("3. Exit");
            System.out.print("Select: ");
            
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                customerLogin();
            } else if (choice.equals("2")) {
                technicianLogin();
            } else if (choice.equals("3")) {
                System.out.println("Thank you for using ATM. Goodbye!");
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ===== CUSTOMER FLOW =====

    private void customerLogin() {
        System.out.print("\nEnter card number: ");
        String card = sc.nextLine().trim();
        System.out.print("Enter PIN: ");
        String pin = sc.nextLine().trim();

        Account acc = service.login(card, pin);

        if (acc == null) {
            System.out.println("Login failed! Invalid card or PIN.");
            return;
        }

        System.out.println("\nWelcome! Login successful.");
        customerMenu(acc);
    }

    private void customerMenu(Account account) {
        while (true) {
            System.out.println("\n===== CUSTOMER MENU =====");
            System.out.println("1. Withdraw");
            System.out.println("2. Deposit");
            System.out.println("3. Transfer");
            System.out.println("4. Balance");
            System.out.println("5. Exit");
            System.out.print("Select: ");
            
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                withdraw(account);
            } else if (choice.equals("2")) {
                deposit(account);
            } else if (choice.equals("3")) {
                transfer(account);
            } else if (choice.equals("4")) {
                checkBalance(account);
            } else if (choice.equals("5")) {
                System.out.println("Thank you for using ATM!");
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void withdraw(Account account) {
        System.out.print("\nEnter amount to withdraw: ");
        try {
            double amount = Double.parseDouble(sc.nextLine().trim());
            boolean success = service.withdraw(account, amount);
            if (success) {
                System.out.print("Print receipt? (yes/no): ");
                String receipt = sc.nextLine().trim().toLowerCase();
                if (receipt.equals("yes") || receipt.equals("y")) {
                    service.printReceipt("WITHDRAW", amount, account.getBalance());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
        }
    }

    private void deposit(Account account) {
        System.out.print("\nEnter amount to deposit: ");
        try {
            double amount = Double.parseDouble(sc.nextLine().trim());
            boolean success = service.deposit(account, amount);
            if (success) {
                System.out.print("Print receipt? (yes/no): ");
                String receipt = sc.nextLine().trim().toLowerCase();
                if (receipt.equals("yes") || receipt.equals("y")) {
                    service.printReceipt("DEPOSIT", amount, account.getBalance());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
        }
    }

    private void transfer(Account account) {
        System.out.print("\nEnter target card number: ");
        String targetCard = sc.nextLine().trim();
        
        System.out.print("Enter amount to transfer: ");
        try {
            double amount = Double.parseDouble(sc.nextLine().trim());
            boolean success = service.transfer(account, targetCard, amount);
            if (success) {
                System.out.print("Print receipt? (yes/no): ");
                String receipt = sc.nextLine().trim().toLowerCase();
                if (receipt.equals("yes") || receipt.equals("y")) {
                    service.printReceipt("TRANSFER", amount, account.getBalance());
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
        }
    }

    private void checkBalance(Account account) {
        // Refresh account balance
        account = service.getAccountDetails(account.getAccountId());
        System.out.println("\n===== YOUR BALANCE =====");
        System.out.printf("Card: %s\n", account.getCardNumber());
        System.out.printf("Balance: $%.2f\n", account.getBalance());
    }

    // ===== TECHNICIAN FLOW =====

    private void technicianLogin() {
        System.out.print("\nEnter technician code: ");
        String code = sc.nextLine().trim();

        if (!code.equals("TECH123")) {
            System.out.println("Invalid technician code!");
            return;
        }

        System.out.println("\nWelcome Technician!");
        technicianMenu();
    }

    private void technicianMenu() {
        while (true) {
            System.out.println("\n===== TECHNICIAN MENU (READ-ONLY) =====");
            System.out.println("1. View ATM Status");
            System.out.println("2. Exit");
            System.out.print("Select: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                service.viewATMStatus();
            } else if (choice.equals("2")) {
                System.out.println("Technician session ended.");
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
