package com.atm.core;

import com.atm.db.DataInitializer;
import com.atm.service.ATMService;
import com.atm.ui.ATMConsoleUI;

public class ATMApplication {

    public static void main(String[] args) {
        // Initialize test data
        DataInitializer.initializeTestData();
        
        ATMService service = new ATMService();
        ATMConsoleUI ui = new ATMConsoleUI(service);
        ui.start();
    }
}
