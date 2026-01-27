package com.atm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "atm_state")
public class ATMState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cash")
    private double cash;

    @Column(name = "paper")
    private int paper;

    @Column(name = "ink")
    private int ink;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    public ATMState() {}

    // ===== GETTERS =====

    public Long getId() {
        return id;
    }

    public double getCash() {
        return cash;
    }

    public int getPaper() {
        return paper;
    }

    public int getInk() {
        return ink;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    // ===== SETTERS =====

    public void setCash(double cash) {
        this.cash = cash;
    }

    public void setPaper(int paper) {
        this.paper = paper;
    }

    public void setInk(int ink) {
        this.ink = ink;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    // ===== OPERATIONS =====

    public void addCash(double amount) {
        this.cash += amount;
    }

    public void collectCash(double amount) {
        this.cash -= amount;
    }

    public void refillPaper(int amount) {
        this.paper += amount;
    }

    public void refillInk(int amount) {
        this.ink += amount;
    }
}
