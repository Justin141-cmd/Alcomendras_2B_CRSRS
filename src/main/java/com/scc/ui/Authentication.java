package com.scc.ui;

import com.scc.model.User;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.util.InputUtil;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    public Authentication(UserDao userDao, ActivityLogDao logDao, Scanner sc){
        this.userDao = userDao;
        this.logDao = logDao;
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

        String fullname = InputUtil.readRequired(sc, "Full name: ");

        String contact;
        while (true) {
            System.out.print("Contact number (11 digits, e.g. 09171234567): ");
            contact = sc.nextLine().trim();
            if (isValidContact(contact)) break;
            System.out.println(" -> [ERROR] It must be exactly 11 digits (numbers only).");
        }

        String username;
        while (true) {
            System.out.print("Username: ");
            username = sc.nextLine().trim();
            if (username.length() < 4 || username.contains(" ")) {
                System.out.println(" -> [ERROR] Username must be at least 4 characters and have no spaces.");
            } else if (userDao.usernameExists(username)) {
                System.out.println(" -> [ERROR] Username already taken.");
            } else {
                break;
            }
        }

        String password;
        while (true) {
            System.out.print("Password (at least 6 characters): ");
            password = sc.nextLine().trim();
            if (password.length() >= 6) break;
            System.out.println(" -> [ERROR] Password must be at least 6 characters.");
        }

        String role;
        while (true) {
            System.out.println("Select role:");
            System.out.println("1. Customer");
            System.out.println("2. Technician");
            System.out.println("3. Admin");
            System.out.print("Enter choice: ");
            String roleChoice = sc.nextLine().trim();
            if (roleChoice.equals("1")) { role = "CUSTOMER"; break; }
            if (roleChoice.equals("2")) { role = "TECHNICIAN"; break; }
            if (roleChoice.equals("3")) { role = "ADMIN"; break; }
            System.out.println(" -> [ERROR] Invalid role choice. Enter 1 to 3 only.");
        }

        if (userDao.registerUser(fullname, contact, username, password, role)) {
            System.out.println("-> Successful Registration, Waiting for Approval");
            // Wala pay user_id (wala pa ma-login), so system log
            logDao.logSystemAction("USER_REGISTERED",
                    "User " + username + " registered as " + role + " (pending approval).");
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
            logDao.logAction(user.user_id(), "LOGIN",
                    "User " + user.username() + " (ID: " + user.user_id() + ") logged in.");
            switch (user.role()){
                case "ADMIN" -> new AdminMenu(userDao, logDao, sc).show(user);
                case "CUSTOMER" -> new CustomerMenu(userDao, logDao,sc).show(user);
                case "TECHNICIAN" -> new TechnicianMenu(userDao, logDao,sc).show(user);
                default ->  System.out.println("Account roles not found!");
            }
            logDao.logAction(user.user_id(), "LOGOUT",
                    "User " + user.username() + " (ID: " + user.user_id() + ") logged out.");
        }else {
            logDao.logSystemAction("FAILED_LOGIN", "Failed login attempt for username: " + username);
        }
    }


    private static boolean isValidContact(String contact) {
        return InputUtil.isValidContact(contact);
    }
}