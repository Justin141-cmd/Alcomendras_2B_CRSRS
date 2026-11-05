package com.scc.ui;

import com.scc.Model.ActivityLog;
import com.scc.Model.RepairRequest;
import com.scc.Model.ServiceType;
import com.scc.Model.User;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.RepairRequestDao;
import com.scc.dao.ServiceTypeDao;
import com.scc.dao.UserDao;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner sc;
    private final UserDao userdao;
    private final ActivityLogDao logDao;
    private final ServiceTypeDao serviceDao = new ServiceTypeDao();
    private final RepairRequestDao requestDao = new RepairRequestDao();

    public AdminMenu(UserDao userdao, ActivityLogDao logDao, Scanner sc) {
        this.sc = sc;
        this.logDao = logDao;
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
                case "1" -> manageUsers(admin);
                case "2" -> manageServiceTypes(admin);
                case "3" -> assignTechnician(admin);
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

    // ==================== MANAGE USERS ====================

    private void manageUsers(User admin) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("         MANAGE USERS");
            System.out.println("===============================");
            System.out.println("[1] Add User\n[2] View All Users\n[3] View Pending User\n[4] Approved User" +
                    "\n[5] Update User(ROLE/STATUS) \n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addUser(admin);
                case "2" -> viewAllUser();
                case "3" -> viewPendingUser();
                case "4" -> approvedUser(admin);
                case "5" -> updateUser(admin);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid choice! 0 to 5 only.");
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

    // Simple nga list para sa pagpili sa technician: ID, Name, Contact ra
    private void displayTechnicianTable(List<User> technicians, String title) {
        System.out.println("\n============== " + title + " ==============");

        if (technicians.isEmpty()) {
            System.out.println("\n-> No Records.");
            return;
        }

        String border = "+------+---------------------------+-----------------+";
        String format = "| %-4s | %-25s | %-15s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "ID", "Name", "Contact");
        System.out.println(border);

        for (User u : technicians) {
            System.out.printf(format, u.user_id(), cut(u.fullname(), 25), u.contact());
        }

        System.out.println(border);
    }

    private void addUser(User admin) {
        System.out.println("\n--- ADD USER ---");

        System.out.print("Full name: ");
        String fullname = sc.nextLine().trim();
        if (fullname.isEmpty()) {
            System.out.println(" -> [ERROR] Full name is required.");
            return;
        }

        System.out.print("Contact number (11 digits, e.g. 09171234567): ");
        String contact = sc.nextLine().trim();
        if (!contact.matches("\\d{11}")) {
            System.out.println(" -> [ERROR] Contact must be exactly 11 digits (numbers only).");
            return;
        }

        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        if (username.length() < 4 || username.contains(" ")) {
            System.out.println(" -> [ERROR] Username must be at least 4 characters and have no spaces.");
            return;
        }
        if (userdao.usernameExists(username)) {
            System.out.println(" -> [ERROR] Username already taken.");
            return;
        }

        System.out.print("Password (at least 6 characters): ");
        String password = sc.nextLine().trim();
        if (password.length() < 6) {
            System.out.println(" -> [ERROR] Password must be at least 6 characters.");
            return;
        }

        String role = readvalidRole();
        if (role == null) {
            System.out.println(" -> Cancelled.");
            return;
        }

        String status = readValidStatus();
        if (status == null) {
            System.out.println(" -> Cancelled.");
            return;
        }

        if (userdao.addUser(fullname, contact, username, password, role, status)) {
            System.out.println(" -> " + role + " account created successfully! (Status: " + status + ")");
            logDao.addLog(admin.user_id(), null, "USER_CREATED: " + username, null,
                    "ROLE=" + role + ", STATUS=" + status);
        }
    }

    private void viewAllUser() {
        displayUserTable(userdao.getAllUser(), " ALL REGISTERED USER");
    }

    private void viewPendingUser() {
        displayUserTable(userdao.getPendingUser(), " PENDING APPROVALS ");
    }

    private void approvedUser(User admin) {
        List<User> pendingUsers = userdao.getPendingUser();
        displayUserTable(pendingUsers, " PENDING APPROVALS ");

        if (pendingUsers.isEmpty()) {
            return;
        }

        int userID = readInput("Enter User ID to approve (0 to cancel): ");

        if (userID == 0) {
            System.out.println("-> Cancelled.");
            return;
        }

        boolean exist = pendingUsers.stream().anyMatch(u -> u.user_id() == userID);

        if (!exist) {
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        if (userdao.approvedUser(userID)) {
            logDao.addLog(admin.user_id(), null, "USER_APPROVED: ID " + userID, "PENDING", "ACTIVE");
        }
    }

    private void updateUser(User currentAdmin) {
        List<User> users = userdao.getAllUser();
        displayUserTable(users, "SELECT USER TO UPDATE");

        if (users.isEmpty()) {
            return;
        }

        int userId = readInput("Enter user ID to update(0 to cancel): ");

        if (userId == 0) {
            System.out.println(" -> Operation Cancelled");
            return;
        }

        if (userId == currentAdmin.user_id()) {
            System.out.println(" -> [WARNING] Modifying current active Admin is Restricted.");
            return;
        }

        User target = users.stream().filter(u -> u.user_id() == userId).findFirst().orElse(null);
        if (target == null) {
            System.out.println(" -> [ERROR] User ID not found");
            return;
        }

        String newRole = readvalidRole();
        if (newRole == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println(" -> Operation Cancelled.");
            return;
        }

        if (userdao.updateUserAndStatus(userId, newRole, newStatus)) {
            String oldValue = "ROLE=" + target.role() + ", STATUS=" + target.status();
            String newValue = "ROLE=" + newRole + ", STATUS=" + newStatus;

            String action = (newStatus.equals("INACTIVE") && !target.status().equals("INACTIVE"))
                    ? "USER_DEACTIVATED: ID " + userId
                    : "USER_UPDATED: ID " + userId;

            logDao.addLog(currentAdmin.user_id(), null, action, oldValue, newValue);
        }
    }

    private String readvalidRole() {
        while (true) {
            System.out.print("Enter Role (ADMIN/CUSTOMER/TECHNICIAN [0 to cancel]): ");
            String role = sc.nextLine().trim().toUpperCase();

            if (role.equals("0") || role.equals("CANCEL")) {
                return null;
            }

            if (role.equals("ADMIN") || role.equals("CUSTOMER") || role.equals("TECHNICIAN")) {
                return role;
            }

            System.out.println(" -> [ERROR] Invalid Role");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter Status(ACTIVE/INACTIVE [0 to cancel]): ");
            String status = sc.nextLine().trim().toUpperCase();

            if (status.equals("0") || status.equals("CANCEL")) {
                return null;
            }

            if (status.equals("ACTIVE") || status.equals("INACTIVE")) {
                return status;
            }

            System.out.println(" -> [ERROR] Invalid Status. Must be ACTIVE or INACTIVE.");
        }
    }

    private int readInput(String prompt) {
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

    // ==================== MANAGE SERVICE TYPES ====================

    private void manageServiceTypes(User admin) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("      MANAGE SERVICE TYPES");
            System.out.println("===============================");
            System.out.println("[1] Add Service Type\n[2] View All Services\n[3] View Active Services" +
                    "\n[4] Update Service Type\n[5] Archive / Restore Service\n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addServiceType(admin);
                case "2" -> displayServiceTable(serviceDao.getAllServices(), " ALL SERVICE TYPES ");
                case "3" -> displayServiceTable(serviceDao.getActiveServices(), " ACTIVE SERVICE TYPES ");
                case "4" -> updateServiceType(admin);
                case "5" -> archiveServiceType(admin);
                case "0" -> {
                    return;
                }
                default -> System.out.println("-> Invalid choice. 0 to 5 only.");
            }
        }
    }

    private void displayServiceTable(List<ServiceType> services, String title) {
        System.out.println("\n==================================== " + title + " ====================================");

        if (services.isEmpty()) {
            System.out.println("\n-> No Records.");
            return;
        }

        String border = "+------+----------------------+--------------------------------+------------+------+----------+";
        String format = "| %-4s | %-20s | %-30s | %-10s | %-4s | %-8s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "ID", "Service Name", "Description", "Base Price", "Days", "Status");
        System.out.println(border);

        for (ServiceType s : services) {
            System.out.printf(format,
                    s.serviceId(),
                    cut(s.serviceName(), 20),
                    cut(s.description(), 30),
                    String.format("%.2f", s.basePrice()),
                    s.estimatedDays(),
                    s.isActive() ? "ACTIVE" : "ARCHIVED");
        }

        System.out.println(border);
    }

    private void addServiceType(User admin) {
        System.out.println("\n--- ADD SERVICE TYPE ---");

        System.out.print("Service name (e.g. OS Reinstall): ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println(" -> [ERROR] Service name is required.");
            return;
        }
        if (serviceDao.serviceNameExists(name, 0)) {
            System.out.println(" -> [ERROR] Service name already exists.");
            return;
        }

        System.out.print("Description: ");
        String description = sc.nextLine().trim();

        double price = readNumber("Base price: ", false);
        int days = Math.max(1, readNumber("Estimated days: ", false).intValue());

        if (serviceDao.addService(name, description, price, days)) {
            System.out.println(" -> Service type added successfully!");
            logDao.addLog(admin.user_id(), null, "SERVICE_CREATED: " + name, null,
                    "PRICE=" + price + ", DAYS=" + days);
        }
    }

    private void updateServiceType(User admin) {
        List<ServiceType> services = serviceDao.getAllServices();
        displayServiceTable(services, " SELECT SERVICE TO UPDATE ");

        if (services.isEmpty()) {
            return;
        }

        int id = readInput("Enter Service ID to update (0 to cancel): ");
        if (id == 0) {
            System.out.println(" -> Operation Cancelled");
            return;
        }

        ServiceType old = serviceDao.getServiceById(id);
        if (old == null) {
            System.out.println(" -> [ERROR] Service ID not found.");
            return;
        }

        System.out.println("(Press Enter to keep the current value)");

        System.out.print("Service name [" + old.serviceName() + "]: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            name = old.serviceName();
        } else if (serviceDao.serviceNameExists(name, id)) {
            System.out.println(" -> [ERROR] Service name already exists.");
            return;
        }

        System.out.print("Description [" + old.description() + "]: ");
        String description = sc.nextLine().trim();
        if (description.isEmpty()) {
            description = old.description();
        }

        Double price = readNumber("Base price [" + old.basePrice() + "]: ", true);
        if (price == null) price = old.basePrice();

        Double daysInput = readNumber("Estimated days [" + old.estimatedDays() + "]: ", true);
        int days = daysInput == null ? old.estimatedDays() : Math.max(1, daysInput.intValue());

        if (serviceDao.updateService(id, name, description, price, days)) {
            System.out.println(" -> Service type updated successfully!");
            logDao.addLog(admin.user_id(), null, "SERVICE_UPDATED: ID " + id,
                    "NAME=" + old.serviceName() + ", PRICE=" + old.basePrice() + ", DAYS=" + old.estimatedDays(),
                    "NAME=" + name + ", PRICE=" + price + ", DAYS=" + days);
        }
    }

    private void archiveServiceType(User admin) {
        List<ServiceType> services = serviceDao.getAllServices();
        displayServiceTable(services, " ARCHIVE / RESTORE SERVICE ");

        if (services.isEmpty()) {
            return;
        }

        int id = readInput("Enter Service ID (0 to cancel): ");
        if (id == 0) {
            System.out.println(" -> Operation Cancelled");
            return;
        }

        ServiceType service = serviceDao.getServiceById(id);
        if (service == null) {
            System.out.println(" -> [ERROR] Service ID not found.");
            return;
        }

        boolean makeActive = !service.isActive();
        if (serviceDao.setActive(id, makeActive)) {
            String oldStatus = service.isActive() ? "ACTIVE" : "ARCHIVED";
            String newStatus = makeActive ? "ACTIVE" : "ARCHIVED";
            System.out.println(" -> Service is now " + newStatus + ".");
            logDao.addLog(admin.user_id(), null,
                    (makeActive ? "SERVICE_RESTORED: ID " : "SERVICE_ARCHIVED: ID ") + id,
                    oldStatus, newStatus);
        }
    }

    // allowBlank = true: mo-return og null kung Enter ra ang gi-press
    private Double readNumber(String prompt, boolean allowBlank) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.isEmpty() && allowBlank) {
                return null;
            }

            try {
                double value = Double.parseDouble(input);
                if (value < 0) {
                    System.out.println(" -> [ERROR] Must not be negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println(" -> [ERROR] Please enter a valid number.");
            }
        }
    }

    // ==================== ASSIGN TECHNICIAN ====================

    private void assignTechnician(User admin) {
        List<RepairRequest> requests = requestDao.getAssignableRequests();
        displayRequestTable(requests, " ASSIGN / REASSIGN TECHNICIAN ");

        if (requests.isEmpty()) {
            return;
        }

        // 1. Pilia ang ticket
        int ticketId = readTicket("\nEnter Ticket No. (0 to cancel): ");
        if (ticketId == 0) {
            System.out.println(" -> Cancelled.");
            return;
        }

        RepairRequest selected = requests.stream()
                .filter(r -> r.requestId() == ticketId)
                .findFirst()
                .orElse(null);

        if (selected == null) {
            System.out.println(" -> [ERROR] Ticket not found in the list.");
            return;
        }

        // 2. Pilia ang technician (active ra, ID/Name/Contact ra ang makita)
        List<User> technicians = userdao.getActiveTechnicians();
        if (technicians.isEmpty()) {
            System.out.println(" -> [ERROR] No active technicians available.");
            return;
        }

        displayTechnicianTable(technicians, " ACTIVE TECHNICIANS ");

        int techId = readInput("\nEnter Technician ID (0 to cancel): ");
        if (techId == 0) {
            System.out.println(" -> Cancelled.");
            return;
        }

        User technician = technicians.stream()
                .filter(u -> u.user_id() == techId)
                .findFirst()
                .orElse(null);

        if (technician == null) {
            System.out.println(" -> [ERROR] Technician ID not found in the list.");
            return;
        }

        if (selected.technicianId() != null && selected.technicianId() == techId) {
            System.out.println(" -> [ERROR] " + technician.fullname() + " is already assigned to this ticket.");
            return;
        }

        // 3. Confirm
        boolean isReassign = selected.technicianId() != null;
        System.out.print((isReassign ? "Reassign " : "Assign ") + selected.ticketNumber()
                + " to " + technician.fullname() + "? (Y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println(" -> Cancelled.");
            return;
        }

        // 4. Save + log
        if (requestDao.assignTechnician(selected.requestId(), techId)) {
            System.out.println(" -> " + selected.ticketNumber() + " is now assigned to " + technician.fullname() + ".");

            String oldValue = isReassign ? "TECH=" + selected.technicianName() : "TECH=Unassigned";
            String newValue = "TECH=" + technician.fullname();
            logDao.addLog(admin.user_id(), selected.requestId(),
                    isReassign ? "TECHNICIAN_REASSIGNED" : "TECHNICIAN_ASSIGNED",
                    oldValue, newValue);
        } else {
            System.out.println(" -> [ERROR] Unable to assign. The request may already be completed or cancelled.");
        }
    }

    // ==================== VIEW ALL REQUESTS ====================

    private void viewAllRequests() {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("      VIEW ALL REQUESTS");
            System.out.println("===============================");
            System.out.println("[1] View All Requests\n[2] Search by Ticket Number\n[3] Search by Customer Name" +
                    "\n[4] Filter by Status\n[5] Filter by Date Submitted\n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> displayRequestTable(requestDao.getAllRequests(), " ALL REPAIR REQUESTS ");
                case "2" -> {
                    int id = readTicket("Enter Ticket No. (0 to cancel): ");
                    if (id != 0) {
                        RepairRequest r = requestDao.getRequestById(id);
                        displayRequestTable(r == null ? List.of() : List.of(r), " SEARCH RESULT ");
                    }
                }
                case "3" -> {
                    System.out.print("Enter Customer Name (0 to cancel): ");
                    String name = sc.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println(" -> [ERROR] Name is required.");
                    } else if (!name.equals("0")) {
                        displayRequestTable(requestDao.searchByCustomerName(name),
                                " RESULTS FOR \"" + cut(name, 20) + "\" ");
                    }
                }
                case "4" -> {
                    String status = readRequestStatus();
                    if (status != null) {
                        displayRequestTable(requestDao.getRequestsByStatus(status),
                                " STATUS: " + status + " ");
                    }
                }
                case "5" -> {
                    String date = readDate("Enter Date (YYYY-MM-DD, 0 to cancel): ");
                    if (date != null) {
                        displayRequestTable(requestDao.getRequestsByDate(date),
                                " SUBMITTED ON " + date + " ");
                    }
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("-> Invalid choice. 0 to 5 only.");
            }
        }
    }

    // Menu para dili na mag-type, aron walay typo. Mo-return og null kung cancel.
    private String readRequestStatus() {
        while (true) {
            System.out.println("\nStatus:");
            System.out.println("  [1] PENDING");
            System.out.println("  [2] DIAGNOSED");
            System.out.println("  [3] IN_REPAIR");
            System.out.println("  [4] READY_FOR_PICKUP");
            System.out.println("  [5] COMPLETED");
            System.out.println("  [6] CANCELLED");
            System.out.println("  [0] Cancel");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> { return "PENDING"; }
                case "2" -> { return "DIAGNOSED"; }
                case "3" -> { return "IN_REPAIR"; }
                case "4" -> { return "READY_FOR_PICKUP"; }
                case "5" -> { return "COMPLETED"; }
                case "6" -> { return "CANCELLED"; }
                case "0" -> { return null; }
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 6 only.");
            }
        }
    }

    // Mo-validate sa format sa petsa. Mo-return og null kung cancel.
    private String readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            try {
                return LocalDate.parse(input).toString();   // YYYY-MM-DD
            } catch (DateTimeParseException e) {
                System.out.println(" -> [ERROR] Invalid date. Use YYYY-MM-DD (e.g. 2026-11-05).");
            }
        }
    }

    private void displayRequestTable(List<RepairRequest> requests, String title) {
        System.out.println("\n=========================================== " + title + " ===========================================");

        if (requests.isEmpty()) {
            System.out.println("\n-> No Records.");
            return;
        }

        String border = "+----------+------------------+----------------------+------------------+----------+------------------+------------------+------------+";
        String format = "| %-8s | %-16s | %-20s | %-16s | %-8s | %-16s | %-16s | %-10s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "Ticket", "Customer", "Device", "Service", "Priority", "Status", "Technician", "Date");
        System.out.println(border);

        for (RepairRequest r : requests) {
            System.out.printf(format,
                    r.ticketNumber(),
                    cut(r.customerName(), 16),
                    cut(r.deviceType() + " " + r.brandModel(), 20),
                    cut(r.serviceName(), 16),
                    r.priority(),
                    r.status(),
                    r.technicianName() == null ? "Unassigned" : cut(r.technicianName(), 16),
                    r.dateSubmitted() == null ? "-" : r.dateSubmitted().substring(0, 10));
        }

        System.out.println(border);
        System.out.println(" Total requests: " + requests.size());
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

    // ==================== REPORTS ====================

    private void reports() {
        System.out.println("\n=== REPORTS ===");
    }

    // ==================== ACTIVITY LOGS ====================

    private void activityLogs() {
        while (true) {
            System.out.println("\n===============================");
            System.out.println("         ACTIVITY LOGS");
            System.out.println("===============================");
            System.out.println("[1] View All Logs\n[2] View Logs by User ID\n[3] View Logs by Request ID\n[0] Back");
            System.out.print("Enter Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> displayLogTable(logDao.getAllLogs(), " ALL ACTIVITY LOGS ");
                case "2" -> {
                    int userId = readInput("Enter User ID: ");
                    displayLogTable(logDao.getLogsByUser(userId), " LOGS OF USER " + userId + " ");
                }
                case "3" -> {
                    int requestId = readInput("Enter Request ID: ");
                    displayLogTable(logDao.getLogsByRequest(requestId), " LOGS OF REQUEST " + requestId + " ");
                }
                case "0" -> {
                    return;
                }
                default -> System.out.println("-> Invalid choice. 0 to 3 only.");
            }
        }
    }

    private void displayLogTable(List<ActivityLog> logs, String title) {
        System.out.println("\n=============================================== " + title + " ===============================================");

        if (logs.isEmpty()) {
            System.out.println("\n-> No Records.");
            return;
        }

        String border = "+------+---------------------+---------+---------+------------------------------+----------------------+----------------------+";
        String format = "| %-4s | %-19s | %-7s | %-7s | %-28s | %-20s | %-20s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "ID", "Timestamp", "User", "Request", "Action", "Old Value", "New Value");
        System.out.println(border);

        for (ActivityLog l : logs) {
            System.out.printf(format,
                    l.logId(),
                    l.logTimestamp(),
                    l.userId() == null ? "N/A" : l.userId(),
                    l.requestId() == null ? "N/A" : l.requestId(),
                    cut(l.action(), 28),
                    cut(l.oldValue(), 20),
                    cut(l.newValue(), 20));
        }

        System.out.println(border);
    }

    // Putlon ang taas nga text para dili mabungkag ang table
    private String cut(String text, int max) {
        if (text == null) return "-";
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}