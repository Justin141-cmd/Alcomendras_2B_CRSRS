package com.scc.ui;

import com.scc.Model.User;
import com.scc.dao.UserDao;

import java.util.Scanner;

public class TechnicianMenu {

    private final UserDao userdao;
    private final Scanner sc;

    public TechnicianMenu(UserDao userdao, Scanner sc) {
        this.sc = sc;
        this.userdao = userdao;
    }

    public void show(User technician) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n--- TECHNICIAN MENU ---");
            System.out.println("1. View Assigned Jobs");
            System.out.println("2. Update Repair Status");
            System.out.println("3. Add Diagnosis / Cost");
            System.out.println("4. Edit Profile / Change Password");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> viewAssignedJobs();
                case "2" -> updateRepairStatus();
                case "3" -> addDiagnosisAndCost();
                case "4" -> editProfile();
                case "0" -> {
                    System.out.println("You have been logged out. Goodbye, " + technician.fullname() + "!");
                    loggedIn = false;
                }
                default -> System.out.println("-> Invalid choice. Try again.");
            }
        }
    }

    private void viewAssignedJobs() {
        System.out.println("\n=== ASSIGNED JOBS ===");

    }

    private void updateRepairStatus() {
        System.out.println("\n=== UPDATE REPAIR STATUS ===");
    }

    private void addDiagnosisAndCost() {
        System.out.println("\n=== DIAGNOSIS / COST ===");
    }

    private void editProfile() {
        System.out.println("\n=== EDIT PROFILE / CHANGE PASSWORD ===");
    }
}