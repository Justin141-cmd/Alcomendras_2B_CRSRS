package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;
import com.scc.util.InputUtil;

import java.util.Scanner;

/** Shared Edit Profile / Change Password screen for Customer and Technician. */
public class ProfileEditor {

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner sc;

    public ProfileEditor(UserDao userDao, ActivityLogDao logDao, Scanner sc) {
        this.userDao = userDao;
        this.logDao = logDao;
        this.sc = sc;
    }

    public void show(User user) {
        while (true) {
            System.out.println("\n[1] Edit Full Name / Contact\n[2] Change Password\n[0] Back");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> editDetails(user);
                case "2" -> changePassword(user);
                case "0" -> {
                    return;
                }
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 2 only.");
            }
        }
    }

    private void editDetails(User user) {
        String fullname = InputUtil.readRequired(sc, "New full name: ");

        String contact;
        while (true) {
            System.out.print("New contact number (11 digits): ");
            contact = sc.nextLine().trim();
            if (InputUtil.isValidContact(contact)) break;
            System.out.println(" -> [ERROR] Contact must be exactly 11 digits (numbers only).");
        }

        if (userDao.updateProfile(user.user_id(), fullname, contact)) {
            System.out.println(" -> Profile updated. Changes show on your next login.");
            logDao.logAction(user.user_id(), "PROFILE_UPDATED",
                    "User " + user.user_id() + " updated their profile details.");
        } else {
            System.out.println(" -> [ERROR] Unable to update profile.");
        }
    }

    private void changePassword(User user) {
        System.out.print("Current password: ");
        String oldPassword = sc.nextLine().trim();

        String newPassword;
        while (true) {
            System.out.print("New password (at least 6 characters): ");
            newPassword = sc.nextLine().trim();
            if (newPassword.length() >= 6) break;
            System.out.println(" -> [ERROR] Password must be at least 6 characters.");
        }

        if (userDao.changePassword(user.user_id(), oldPassword, newPassword)) {
            System.out.println(" -> Password changed successfully.");
            logDao.logAction(user.user_id(), "PASSWORD_CHANGED",
                    "User " + user.user_id() + " changed their password.");
        } else {
            System.out.println(" -> [ERROR] Current password is incorrect.");
        }
    }
}
