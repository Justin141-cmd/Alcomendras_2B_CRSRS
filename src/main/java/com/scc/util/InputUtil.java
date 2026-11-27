package com.scc.util;

import java.util.Scanner;

/**
 * Input validation guardrails: re-prompts until the user enters valid data,
 * so invalid entries never crash the program.
 */
public class InputUtil {

    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid numeric ID.");
            }
        }
    }

    public static String readRequired(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println(" -> [ERROR] This field is required.");
        }
    }

    public static boolean isValidContact(String contact) {
        return contact != null && contact.matches("\\d{11}");
    }
}
