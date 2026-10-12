package com.scc.ui;

import com.scc.Model.User;

import java.util.Scanner;

public class AdminMenu {

    private final Scanner sc;
    private final User user;

    public AdminMenu(Scanner sc, User user) {
        this.sc = sc;
        this.user = user;
    }

    public void show() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Service Types");
            System.out.println("3. Assign Technician");
            System.out.println("4. View All Requests");
            System.out.println("5. Reports");
            System.out.println("6. Activity Logs");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> manageUsers();
                case "2" -> manageServiceTypes();
                case "3" -> assignTechnician();
                case "4" -> viewAllRequests();
                case "5" -> reports();
                case "6" -> activityLogs();
                case "0" -> {
                    System.out.println("You have been logged out. Goodbye, " + user.fullname() + "!");
                    loggedIn = false;
                }
                default -> System.out.println("-> Invalid choice. Try again.");
            }
        }
    }

    private void manageUsers() {
        System.out.println("\n=== MANAGE USERS ===");
        // diri ibutang ang code
    }

    private void manageServiceTypes() {
        System.out.println("\n=== MANAGE SERVICE TYPES ===");
    }

    private void assignTechnician() {
        System.out.println("\n=== ASSIGN TECHNICIAN ===");
    }

    private void viewAllRequests() {
        System.out.println("\n=== ALL REPAIR REQUESTS ===");
    }

    private void reports() {
        System.out.println("\n=== REPORTS ===");
    }

    private void activityLogs() {
        System.out.println("\n=== ACTIVITY LOGS ===");
    }
}