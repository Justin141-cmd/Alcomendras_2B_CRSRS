package com.scc.ui;

import com.scc.Model.RepairRequest;
import com.scc.Model.User;
import com.scc.dao.ActivityLogDao;
import com.scc.dao.RepairRequestDao;
import com.scc.dao.UserDao;

import java.util.List;
import java.util.Scanner;

public class TechnicianMenu {
    private static final int WIDTH = 46;

    private final UserDao userdao;
    private final ActivityLogDao logDao;
    private final RepairRequestDao requestDao = new RepairRequestDao();
    private final Scanner sc;

    public TechnicianMenu(UserDao userdao, ActivityLogDao logDao, Scanner sc) {
        this.sc = sc;
        this.userdao = userdao;
        this.logDao = logDao;
    }

    public void show(User technician) {
        boolean loggedIn = true;

        while (loggedIn) {
            printHeader("TECHNICIAN MENU");
            System.out.println("  Logged in as: " + technician.fullname());
            System.out.println();
            System.out.println("  [1] View Assigned Jobs");
            System.out.println("  [2] Update Repair Status");
            System.out.println("  [3] Add Diagnosis / Cost");
            System.out.println("  [4] Edit Profile / Change Password");
            System.out.println("  [0] Logout");
            System.out.println("-".repeat(WIDTH));
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> viewAssignedJobs(technician);
                case "2" -> updateRepairStatus(technician);
                case "3" -> addDiagnosisAndCost(technician);
                case "4" -> editProfile();
                case "0" -> {
                    System.out.println("\n -> You have been logged out. Goodbye, " + technician.fullname() + "!");
                    loggedIn = false;
                }
                default -> System.out.println(" -> [ERROR] Invalid choice. 0 to 4 only.");
            }
        }
    }

    // ==================== VIEW ASSIGNED JOBS ====================

    private void viewAssignedJobs(User technician) {
        printHeader("ASSIGNED JOBS");

        List<RepairRequest> jobs = requestDao.getRequestsByTechnician(technician.user_id());
        if (jobs.isEmpty()) {
            System.out.println(" -> You have no assigned jobs yet.");
            return;
        }

        displayJobTable(jobs);

        // Optional: tan-awa ang details sa usa ka job
        while (true) {
            int id = readTicket("\nEnter Ticket No. for details (0 to go back): ");
            if (id == 0) {
                return;
            }

            // Iya ra nga jobs ang makita (gikan sa iyang kaugalingong list)
            RepairRequest found = jobs.stream()
                    .filter(r -> r.requestId() == id)
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                System.out.println(" -> [ERROR] Ticket not found in your assigned jobs.");
            } else {
                printJobDetails(found);
            }
        }
    }

    private void displayJobTable(List<RepairRequest> jobs) {
        String border = "+----------+----------------+--------------------+----------------+----------+------------------+------------+------------+";
        String format = "| %-8s | %-14s | %-18s | %-14s | %-8s | %-16s | %-10s | %-10s |\n";

        System.out.println();
        System.out.println(border);
        System.out.printf(format, "Ticket", "Customer", "Device", "Service", "Priority", "Status", "Est. Cost", "Final Cost");
        System.out.println(border);

        for (RepairRequest r : jobs) {
            System.out.printf(format,
                    r.ticketNumber(),
                    cut(r.customerName(), 14),
                    cut(r.deviceType() + " " + r.brandModel(), 18),
                    cut(r.serviceName(), 14),
                    r.priority(),
                    r.status(),
                    money(r.estimatedCost()),
                    money(r.finalCost()));
        }

        System.out.println(border);
        System.out.println(" Total jobs: " + jobs.size());
    }

    private void printJobDetails(RepairRequest r) {
        String border = "+" + "-".repeat(56) + "+";
        String rowFormat = "| %-14s: %-37s |%n";

        System.out.println();
        System.out.println(border);
        System.out.printf("| %-54s |%n", "JOB DETAILS - " + r.ticketNumber());
        System.out.println(border);
        System.out.printf(rowFormat, "Status", r.status());
        System.out.printf(rowFormat, "Customer", cut(r.customerName(), 37));
        System.out.printf(rowFormat, "Service", cut(r.serviceName(), 37));
        System.out.printf(rowFormat, "Device", cut(r.deviceType() + " - " + r.brandModel(), 37));
        System.out.printf(rowFormat, "Priority", r.priority());
        System.out.printf(rowFormat, "Est. Cost", money(r.estimatedCost()));
        System.out.printf(rowFormat, "Final Cost", money(r.finalCost()));
        System.out.printf(rowFormat, "Submitted", r.dateSubmitted() == null ? "-" : r.dateSubmitted());
        System.out.printf(rowFormat, "Completed", r.dateCompleted() == null ? "-" : r.dateCompleted());
        System.out.println(border);

        System.out.println(" Problem   : " + r.problemDescription());
        System.out.println(" Diagnosis : " + (r.diagnosisNotes() == null || r.diagnosisNotes().isBlank()
                ? "No diagnosis yet." : r.diagnosisNotes()));
    }

    // ==================== UPDATE REPAIR STATUS ====================

    private void updateRepairStatus(User technician) {
        printHeader("UPDATE REPAIR STATUS");

        List<RepairRequest> active = getActiveJobs(technician);
        if (active.isEmpty()) {
            System.out.println(" -> You have no active jobs to update.");
            return;
        }

        displayJobTable(active);

        RepairRequest job = pickJob(active, "\nEnter Ticket No. to update (0 to cancel): ");
        if (job == null) {
            return;
        }

        String current = job.status();
        String next = nextStatus(current);
        if (next == null) {
            System.out.println(" -> [ERROR] This ticket cannot be moved forward.");
            return;
        }

        // Rules: kinahanglan naay diagnosis sa dili pa DIAGNOSED, ug final cost sa dili pa COMPLETED
        if (next.equals("DIAGNOSED") && (job.diagnosisNotes() == null || job.diagnosisNotes().isBlank())) {
            System.out.println(" -> [ERROR] Add diagnosis notes first (option 3) before marking as DIAGNOSED.");
            return;
        }
        if (next.equals("COMPLETED") && job.finalCost() == null) {
            System.out.println(" -> [ERROR] Set the final cost first (option 3) before marking as COMPLETED.");
            return;
        }

        System.out.print("Move " + job.ticketNumber() + " from " + current + " to " + next + "? (Y/N): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.println(" -> Cancelled.");
            return;
        }

        if (requestDao.updateStatus(job.requestId(), technician.user_id(), current, next)) {
            System.out.println(" -> " + job.ticketNumber() + " is now " + next + ".");
            logDao.addLog(technician.user_id(), job.requestId(), "STATUS_CHANGE", current, next);
        } else {
            System.out.println(" -> [ERROR] Unable to update. The ticket may have been changed or reassigned.");
        }
    }

    // PENDING -> DIAGNOSED -> IN_REPAIR -> READY_FOR_PICKUP -> COMPLETED
    private String nextStatus(String current) {
        return switch (current) {
            case "PENDING" -> "DIAGNOSED";
            case "DIAGNOSED" -> "IN_REPAIR";
            case "IN_REPAIR" -> "READY_FOR_PICKUP";
            case "READY_FOR_PICKUP" -> "COMPLETED";
            default -> null;
        };
    }

    // ==================== DIAGNOSIS / COST ====================

    private void addDiagnosisAndCost(User technician) {
        printHeader("DIAGNOSIS / COST");

        List<RepairRequest> active = getActiveJobs(technician);
        if (active.isEmpty()) {
            System.out.println(" -> You have no active jobs to update.");
            return;
        }

        displayJobTable(active);

        RepairRequest job = pickJob(active, "\nEnter Ticket No. (0 to cancel): ");
        if (job == null) {
            return;
        }

        System.out.println("\n(Press Enter to keep the current value)");

        // 1. Diagnosis notes
        String oldNotes = job.diagnosisNotes() == null ? "" : job.diagnosisNotes();
        System.out.println("Current diagnosis: " + (oldNotes.isBlank() ? "None" : oldNotes));
        System.out.print("New diagnosis notes: ");
        String notesInput = sc.nextLine().trim();
        String newNotes = notesInput.isEmpty() ? oldNotes : notesInput;

        // 2. Costs
        Double newEstimated = readCost("Estimated cost [" + money(job.estimatedCost()) + "]: ", job.estimatedCost());
        Double newFinal = readCost("Final cost [" + money(job.finalCost()) + "]: ", job.finalCost());

        boolean notesChanged = !newNotes.equals(oldNotes);
        boolean costChanged = !sameAmount(newEstimated, job.estimatedCost()) || !sameAmount(newFinal, job.finalCost());

        if (!notesChanged && !costChanged) {
            System.out.println(" -> No changes made.");
            return;
        }

        if (requestDao.updateDiagnosisAndCost(job.requestId(), technician.user_id(), newNotes, newEstimated, newFinal)) {
            System.out.println(" -> " + job.ticketNumber() + " updated successfully!");

            if (notesChanged) {
                logDao.addLog(technician.user_id(), job.requestId(), "DIAGNOSIS_UPDATED",
                        oldNotes.isBlank() ? null : oldNotes, newNotes);
            }
            if (costChanged) {
                logDao.addLog(technician.user_id(), job.requestId(), "COST_EDIT",
                        "EST=" + money(job.estimatedCost()) + ", FINAL=" + money(job.finalCost()),
                        "EST=" + money(newEstimated) + ", FINAL=" + money(newFinal));
            }
        } else {
            System.out.println(" -> [ERROR] Unable to update. The ticket may be completed, cancelled, or reassigned.");
        }
    }

    // ==================== SOON ====================

    private void editProfile() {
        printHeader("EDIT PROFILE / CHANGE PASSWORD");
    }

    // ==================== JOB HELPERS ====================

    // Jobs nga wala pa COMPLETED o CANCELLED
    private List<RepairRequest> getActiveJobs(User technician) {
        return requestDao.getRequestsByTechnician(technician.user_id()).stream()
                .filter(r -> !r.status().equals("COMPLETED") && !r.status().equals("CANCELLED"))
                .toList();
    }

    // Mo-return sa napili nga job gikan sa list, o null kung cancel/dili valid
    private RepairRequest pickJob(List<RepairRequest> jobs, String prompt) {
        int id = readTicket(prompt);
        if (id == 0) {
            System.out.println(" -> Cancelled.");
            return null;
        }

        RepairRequest job = jobs.stream()
                .filter(r -> r.requestId() == id)
                .findFirst()
                .orElse(null);

        if (job == null) {
            System.out.println(" -> [ERROR] Ticket not found in your active jobs.");
        }
        return job;
    }

    private String money(Double value) {
        return value == null ? "-" : String.format("%.2f", value);
    }

    private boolean sameAmount(Double a, Double b) {
        if (a == null || b == null) {
            return a == null && b == null;
        }
        return Double.compare(a, b) == 0;
    }

    // ==================== INPUT HELPERS ====================

    // Enter ra = ibilin ang karaan nga value (current)
    private Double readCost(String prompt, Double current) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                return current;
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

    // ==================== DISPLAY HELPERS ====================

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