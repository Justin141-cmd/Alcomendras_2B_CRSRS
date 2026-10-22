package com.scc.ui;

import com.scc.Model.User;
import com.scc.dao.UserDao;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, Scanner sc){
        this.userDao = userDao;
        this.sc = sc;
    }

    public void start(){
        boolean running = true;

        while (running) {
            System.out.println("\n==========================================");
            System.out.println("  COMPUTER REPAIR SERVICE REQUEST SYSTEM");
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

    private void register() {
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

    private void login() {
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

        if(user != null){
            System.out.println("\n -> Welcome "+user.fullname()+" | ROLE: "+user.role()+"");
            switch (user.role()){
                case "ADMIN" -> new AdminMenu(userDao, sc).show(user);
                case "CUSTOMER" -> new CustomerMenu(userDao, sc).show(user);
                case "TECHNICIAN" -> new TechnicianMenu(userDao, sc).show(user);
                default ->  System.out.println("Account roles not found!");
            }
        }
    }


    private static boolean isValidContact(String contact) {
        return contact.matches("\\d{11}");
    }
}
