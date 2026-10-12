package com.scc;

import com.scc.Model.User;
import com.scc.dao.UserDao;
import com.scc.ui.AdminMenu;
import com.scc.ui.CustomerMenu;
import com.scc.ui.TechnicianMenu;

import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n==========================================");
            System.out.println("   COMPUTER REPAIR SERVICE REQUEST SYSTEM");
            System.out.println("==========================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("-> Invalid choice. Try again.");
            }
        }
        sc.close();
    }

    private static void register() {
        System.out.println("\n--- REGISTER ---");

        System.out.print("Full name: ");
        String fullname = sc.nextLine().trim();
        if (fullname.isEmpty()) {
            System.out.println("-> Full name is required.");
            return;
        }

        System.out.print("Contact number (11 digits, e.g. 09171234567): ");
        String contact = sc.nextLine().trim();
        if (!isValidContact(contact)) {
            System.out.println("-> Invalid contact number. It must be exactly 11 digits (numbers only).");
            return;
        }

        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        if (username.length() < 4 || username.contains(" ")) {
            System.out.println("-> Username must be at least 4 characters and have no spaces.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("-> Username already taken.");
            return;
        }

        System.out.print("Password (at least 6 characters): ");
        String password = sc.nextLine().trim();
        if (password.length() < 6) {
            System.out.println("-> Password must be at least 6 characters.");
            return;
        }

        System.out.println("Select role:");
        System.out.println("1. Customer");
        System.out.println("2. Technician");
        System.out.println("3. Admin");
        System.out.print("Enter choice: ");
        String roleChoice = sc.nextLine().trim();

        String role;
        switch (roleChoice) {
            case "1" -> role = "CUSTOMER";
            case "2" -> role = "TECHNICIAN";
            case "3" -> role = "ADMIN";
            default -> {
                System.out.println("-> Invalid role choice.");
                return;
            }
        }

        if (userDao.registerUser(fullname, contact, username, password, role)) {
            System.out.println("-> Successful Registration, Waiting for Approval");
        }
    }

    private static void login() {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Username: ");
        String username = sc.nextLine().trim();

        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("-> Username and password are required.");
            return;
        }

        User user = userDao.loginUser(username, password);

        if (user == null) {
            System.out.println("-> Invalid username or password.");
            return;
        }

        switch (user.status().toUpperCase()) {
            case "PENDING" -> System.out.println("-> Your account is still waiting for approval.");
            case "INACTIVE" -> System.out.println("-> Your account has been deactivated.");
            case "ACTIVE" -> {
                System.out.println("-> Login successful! Welcome, " + user.fullname() + " (" + user.role() + ").");
                openMenuByRole(user);
            }
            default -> System.out.println("-> Unknown account status.");
        }
    }

    // Mopili sa menu class (ui package) base sa role
    private static void openMenuByRole(User user) {
        switch (user.role().toUpperCase()) {
            case "ADMIN" -> new AdminMenu(sc, user).show();
            case "TECHNICIAN" -> new TechnicianMenu(sc, user).show();
            case "CUSTOMER" -> new CustomerMenu(sc, user).show();
            default -> System.out.println("-> Unknown role.");
        }
    }

    private static boolean isValidContact(String contact) {
        return contact.matches("\\d{11}");
    }
}