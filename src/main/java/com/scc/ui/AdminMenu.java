package com.scc.ui;

import com.scc.Model.User;
import com.scc.dao.UserDao;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import static java.nio.file.Files.readString;

public class AdminMenu {

    private final Scanner sc;
    private final UserDao userdao;

    public AdminMenu(UserDao userdao, Scanner sc) {
        this.sc = sc;
        this.userdao = userdao;
    }

    public void show(User admin) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n==============================");
            System.out.println("           ADMIN MENU ");
            System.out.println("==============================");
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
                    System.out.println("You have been logged out. Goodbye, " + admin.fullname() + "!");
                    loggedIn = false;
                }
                default -> System.out.println("-> Invalid choice. Try again.");
            }
        }
    }

    private void manageUsers() {
        System.out.println("\n=== MANAGE USERS ===");
        while (true) {
            System.out.println("\n===============================");
            System.out.println("         MANAGE USERS");
            System.out.println("===============================");
            System.out.println("[1] Add User\n[2] View All Users\n[3] View Pending User\n[4] Approved User" +
                    "\n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1" -> System.out.println("Add User (soon)");
                case "2" -> viewAllUser();
                case "3" -> viewPendingUser();
                case "4" -> approvedUser();
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice! 0 to 6 only.");
            }
        }
    }

    private void displayUserTable(List<User> users, String title) {
        System.out.println("\n====================================== " + title + " ======================================");

        if (users.isEmpty()) {
            System.out.println("\n-> No Records.");
            return;
        }

        String border = "+------+--------------------+-----------------+------------------+------------+------------+---------------------+";
        String format = "| %-4s | %-18s | %-15s | %-16s | %-10s | %-10s | %-19s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "ID", "Fullname", "Contact", "Username", "Role", "Status", "Date Created");
        System.out.println(border);

        for (User u : users) {
            System.out.printf(format, u.user_id(), u.fullname(), u.contact(), u.username(),
                    u.role(), u.status(), u.date_created());
        }

        System.out.println(border);
    }

    private void viewAllUser(){
        displayUserTable(userdao.getAllUser(), " ALL REGISTERED USER");
    }

    private void viewPendingUser(){
        displayUserTable(userdao.getPendingUser(), " PENDING APPROVALS ");
    }

    private void approvedUser(){
        List<User> pendingUsers = userdao.getPendingUser();
        displayUserTable(pendingUsers, " PENDING APPROVALS ");

        if(pendingUsers.isEmpty()){
            return;
        }

        int chooseId;

        while(true){
            System.out.print("Enter User ID to approved (0 to cancel): ");
            try{
                chooseId = sc.nextInt();
                sc.nextLine();
                break;
            }catch (InputMismatchException e){
                System.out.println("-> Invalid input! Numbers only.");
                sc.nextLine();
                return;
            }
        }

        if(chooseId == 0){
            System.out.println("-> Cancelled.");
            return;
        }

        int userID = chooseId;

        boolean exist = pendingUsers.stream().anyMatch(u -> u.user_id() == userID);

        if(!exist){
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        userdao.approvedUser(userID);
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