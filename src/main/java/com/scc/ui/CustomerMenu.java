package com.scc.ui;

import com.scc.Model.User;

import java.util.Scanner;

public class CustomerMenu {

    private final Scanner sc;
    private final User user;

    public CustomerMenu(Scanner sc, User user) {
        this.sc = sc;
        this.user = user;
    }

    public void show() {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n--- CUSTOMER MENU ---");
            System.out.println("1. Submit Repair Request");
            System.out.println("2. View My Requests");
            System.out.println("3. Cancel Pending Request");
            System.out.println("4. Edit Profile / Change Password");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> submitRequest();
                case "2" -> viewMyRequests();
                case "3" -> cancelPendingRequest();
                case "4" -> editProfile();
                case "0" -> {
                    System.out.println("You have been logged out. Goodbye, " + user.fullname() + "!");
                    loggedIn = false;
                }
                default -> System.out.println("-> Invalid choice. Try again.");
            }
        }
    }

    private void submitRequest() {
        System.out.println("\n=== SUBMIT REPAIR REQUEST ===");
        // diri ibutang ang code
    }

    private void viewMyRequests() {
        System.out.println("\n=== MY REQUESTS ===");
    }

    private void cancelPendingRequest() {
        System.out.println("\n=== CANCEL PENDING REQUEST ===");
    }

    private void editProfile() {
        System.out.println("\n=== EDIT PROFILE / CHANGE PASSWORD ===");
    }
}