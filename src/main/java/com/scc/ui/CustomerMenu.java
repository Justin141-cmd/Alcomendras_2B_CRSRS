package com.scc.ui;

import com.scc.Model.RepairRequest;
import com.scc.Model.ServiceType;
import com.scc.Model.User;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.RepairRequestDao;
import com.scc.dao.ServiceTypeDao;
import com.scc.dao.UserDao;

import java.util.List;
import java.util.Scanner;

public class CustomerMenu {
    private static final int WIDTH = 46;

    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final RepairRequestDao requestDao = new RepairRequestDao();
    private final ServiceTypeDao serviceDao = new ServiceTypeDao();
    private final Scanner sc;

    public CustomerMenu(UserDao userDao, ActivityLogDao logDao, Scanner sc) {
        this.sc = sc;
        this.userDao = userDao;
        this.logDao = logDao;
    }

    public void show(User customer) {
        boolean loggedIn = true;

        while (loggedIn) {
            printHeader("CUSTOMER MENU");
            System.out.println("  Logged in as: " + customer.fullname());
            System.out.println();
            System.out.println("  [1] Submit Repair Request");
            System.out.println("  [2] View My Requests");
            System.out.println("  [3] Cancel Pending Request");
            System.out.println("  [4] Edit Profile / Change Password");
            System.out.println("  [0] Logout");
            System.out.println("-".repeat(WIDTH));
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> submitRequest(customer);
                case "2" -> viewMyRequests(customer);
                case "3" -> cancelPendingRequest(customer);
                case "4" -> editProfile();
                case "0" -> {
                    System.out.println("\n -> You have been logged out. Goodbye, " + customer.fullname() + "!");
                    loggedIn = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 4 only.");
            }
        }
    }

    // ---------- Submit Request ----------

    private void submitRequest(User customer) {
        printHeader("SUBMIT REPAIR REQUEST");

        // 1. Pilia ang service (active ra ang makita)
        List<ServiceType> services = serviceDao.getActiveServices();
        if (services.isEmpty()) {
            System.out.println(" -> No services available right now. Please try again later.");
            return;
        }

        displayServiceTable(services);

        ServiceType selected = null;
        while (selected == null) {
            int serviceId = readInput("\nEnter Service ID (0 to cancel): ");
            if (serviceId == 0) {
                System.out.println(" -> Cancelled.");
                return;
            }
            selected = services.stream()
                    .filter(s -> s.serviceId() == serviceId)
                    .findFirst()
                    .orElse(null);
            if (selected == null) {
                System.out.println(" -> [ERROR] Service ID not in the list.");
            }
        }

        // 2. Device type
        String deviceType = readDeviceType();
        if (deviceType == null) {
            System.out.println(" -> Cancelled.");
            return;
        }

        // 3. Brand/model ug problema
        System.out.println();
        String brandModel = readRequired("Brand / Model (e.g. Acer Aspire 5): ");
        if (brandModel == null) return;

        String problem = readRequired("Describe the problem: ");
        if (problem == null) return;

        // 4. Priority
        String priority = readPriority();
        if (priority == null) {
            System.out.println(" -> Cancelled.");
            return;
        }

        // 5. Save
        int requestId = requestDao.submitRequest(customer.user_id(), selected.serviceId(),
                deviceType, brandModel, problem, priority);

        if (requestId > 0) {
            logDao.logAction(customer.user_id(), "TICKET_CREATED",
                    "Submitted " + String.format("TKT-%04d", requestId)
                            + " (" + selected.serviceName() + ", " + priority + ")");
            printReceipt(requestId, selected, deviceType, brandModel, priority);
        } else {
            System.out.println(" -> [ERROR] Failed to submit request.");
        }
    }

    private void displayServiceTable(List<ServiceType> services) {
        String border = "+------+----------------------+--------------+------+";
        String format = "| %-4s | %-20s | %-12s | %-4s |\n";

        System.out.println("\nAvailable Services:");
        System.out.println(border);
        System.out.printf(format, "ID", "Service", "Base Price", "Days");
        System.out.println(border);

        for (ServiceType s : services) {
            System.out.printf(format,
                    s.serviceId(),
                    cut(s.serviceName(), 20),
                    "PHP " + String.format("%.2f", s.basePrice()),
                    s.estimatedDays());
        }

        System.out.println(border);
    }

    private void printReceipt(int requestId, ServiceType service, String deviceType,
                              String brandModel, String priority) {
        String border = "+" + "-".repeat(42) + "+";
        String rowFormat = "| %-11s: %-27s |%n";

        System.out.println();
        System.out.println(border);
        System.out.printf("| %-40s |%n", "REQUEST SUBMITTED SUCCESSFULLY!");
        System.out.println(border);
        System.out.printf(rowFormat, "Ticket No.", String.format("TKT-%04d", requestId));
        System.out.printf(rowFormat, "Service", cut(service.serviceName(), 27));
        System.out.printf(rowFormat, "Device", cut(deviceType + " - " + brandModel, 27));
        System.out.printf(rowFormat, "Priority", priority);
        System.out.printf(rowFormat, "Status", "PENDING");
        System.out.printf(rowFormat, "Est. Cost", "PHP " + String.format("%.2f", service.basePrice()));
        System.out.println(border);
        System.out.println(" Please keep your ticket number for tracking.");
    }

    // ---------- View My Requests ----------

    private void viewMyRequests(User customer) {
        printHeader("MY REQUESTS");

        List<RepairRequest> requests = requestDao.getRequestsByCustomer(customer.user_id());
        if (requests.isEmpty()) {
            System.out.println(" -> You have no repair requests yet.");
            return;
        }

        displayRequestTable(requests);

        // Optional: tan-awa ang details sa usa ka request
        while (true) {
            int id = readTicket("\nEnter Ticket No. for details (0 to go back): ");
            if (id == 0) {
                return;
            }

            // Iya ra nga request ang makita (gikan sa iyang kaugalingong list)
            RepairRequest found = requests.stream()
                    .filter(r -> r.requestId() == id)
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                System.out.println(" -> [ERROR] Ticket not found in your requests.");
            } else {
                printRequestDetails(found);
            }
        }
    }

    private void displayRequestTable(List<RepairRequest> requests) {
        String border = "+----------+------------------+----------------------+----------+------------------+-------------+------------+";
        String format = "| %-8s | %-16s | %-20s | %-8s | %-16s | %-11s | %-10s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "Ticket", "Service", "Device", "Priority", "Status", "Cost (PHP)", "Date");
        System.out.println(border);

        for (RepairRequest r : requests) {
            System.out.printf(format,
                    r.ticketNumber(),
                    cut(r.serviceName(), 16),
                    cut(r.deviceType() + " " + r.brandModel(), 20),
                    r.priority(),
                    r.status(),
                    formatCost(r),
                    r.dateSubmitted() == null ? "-" : r.dateSubmitted().substring(0, 10));
        }

        System.out.println(border);
        System.out.println(" Total requests: " + requests.size());
    }

    private void printRequestDetails(RepairRequest r) {
        String border = "+" + "-".repeat(56) + "+";
        String rowFormat = "| %-14s: %-37s |%n";

        System.out.println();
        System.out.println(border);
        System.out.printf("| %-54s |%n", "REQUEST DETAILS - " + r.ticketNumber());
        System.out.println(border);
        System.out.printf(rowFormat, "Status", r.status());
        System.out.printf(rowFormat, "Service", cut(r.serviceName(), 37));
        System.out.printf(rowFormat, "Device", cut(r.deviceType() + " - " + r.brandModel(), 37));
        System.out.printf(rowFormat, "Priority", r.priority());
        System.out.printf(rowFormat, "Technician", r.technicianName() == null ? "Not yet assigned" : cut(r.technicianName(), 37));
        System.out.printf(rowFormat, "Est. Cost", r.estimatedCost() == null ? "-" : "PHP " + String.format("%.2f", r.estimatedCost()));
        System.out.printf(rowFormat, "Final Cost", r.finalCost() == null ? "-" : "PHP " + String.format("%.2f", r.finalCost()));
        System.out.printf(rowFormat, "Submitted", r.dateSubmitted() == null ? "-" : r.dateSubmitted());
        System.out.printf(rowFormat, "Completed", r.dateCompleted() == null ? "-" : r.dateCompleted());
        System.out.println(border);

        System.out.println(" Problem   : " + r.problemDescription());
        System.out.println(" Diagnosis : " + (r.diagnosisNotes() == null || r.diagnosisNotes().isBlank()
                ? "No diagnosis yet." : r.diagnosisNotes()));
    }

    // Final cost kung naa na, kung wala estimated cost
    private String formatCost(RepairRequest r) {
        if (r.finalCost() != null) return String.format("%.2f", r.finalCost());
        if (r.estimatedCost() != null) return String.format("~%.2f", r.estimatedCost());
        return "-";
    }

    // ---------- Cancel Pending Request ----------

    private void cancelPendingRequest(User customer) {
        printHeader("CANCEL PENDING REQUEST");

        // Pending ra ang pwede i-cancel
        List<RepairRequest> pending = requestDao.getRequestsByCustomer(customer.user_id()).stream()
                .filter(r -> "PENDING".equals(r.status()))
                .toList();

        if (pending.isEmpty()) {
            System.out.println(" -> You have no pending requests to cancel.");
            return;
        }

        displayRequestTable(pending);

        int id = readTicket("\nEnter Ticket No. to cancel (0 to go back): ");
        if (id == 0) {
            System.out.println(" -> Cancelled.");
            return;
        }

        boolean isPending = pending.stream().anyMatch(r -> r.requestId() == id);
        if (!isPending) {
            System.out.println(" -> [ERROR] Ticket not found in your pending requests.");
            return;
        }

        System.out.print("Are you sure you want to cancel " + String.format("TKT-%04d", id) + "? (Y/N): ");
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println(" -> Request not cancelled.");
            return;
        }

        if (requestDao.cancelRequest(id, customer.user_id())) {
            System.out.println(" -> " + String.format("TKT-%04d", id) + " has been cancelled.");
            logDao.logAction(customer.user_id(), "TICKET_CANCELLED",
                    "Cancelled " + String.format("TKT-%04d", id) + " status from PENDING to CANCELLED");
        } else {
            System.out.println(" -> [ERROR] Unable to cancel. The request may no longer be pending.");
        }
    }

    // ---------- Soon ----------

    private void editProfile() {
        printHeader("EDIT PROFILE / CHANGE PASSWORD");
    }

    // ---------- Input helpers ----------

    private String readDeviceType() {
        while (true) {
            System.out.println("\nDevice Type:");
            System.out.println("  [1] Laptop");
            System.out.println("  [2] Desktop PC");
            System.out.println("  [3] All-in-One");
            System.out.println("  [4] Other");
            System.out.println("  [0] Cancel");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> { return "LAPTOP"; }
                case "2" -> { return "DESKTOP"; }
                case "3" -> { return "ALL-IN-ONE"; }
                case "4" -> { return "OTHER"; }
                case "0" -> { return null; }
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 4 only.");
            }
        }
    }

    private String readPriority() {
        while (true) {
            System.out.println("\nPriority:");
            System.out.println("  [1] Low");
            System.out.println("  [2] Normal");
            System.out.println("  [3] High");
            System.out.println("  [4] Urgent");
            System.out.println("  [0] Cancel");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> { return "LOW"; }
                case "2" -> { return "NORMAL"; }
                case "3" -> { return "HIGH"; }
                case "4" -> { return "URGENT"; }
                case "0" -> { return null; }
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 4 only.");
            }
        }
    }

    // Mo-return og null kung empty ang input
    private String readRequired(String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isEmpty()) {
            System.out.println(" -> [ERROR] This field is required. Request cancelled.");
            return null;
        }
        return input;
    }

    private int readInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid number.");
            }
        }
    }

    // Madawat ang "5", "0005", o "TKT-0005"
    private int readTicket(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim().toUpperCase();

            if (input.startsWith("TKT-")) {
                input = input.substring(4);
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Enter a ticket number like 5 or TKT-0005.");
            }
        }
    }

    // ---------- Display helpers ----------

    private void printHeader(String title) {
        int pad = Math.max(0, (WIDTH - title.length()) / 2);
        System.out.println("\n" + "=".repeat(WIDTH));
        System.out.println(" ".repeat(pad) + title);
        System.out.println("=".repeat(WIDTH));
    }

    // Putlon ang taas nga text para dili mabungkag ang table
    private String cut(String text, int max) {
        if (text == null) return "-";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}