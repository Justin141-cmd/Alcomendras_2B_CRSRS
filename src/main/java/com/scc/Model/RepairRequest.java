package com.scc.Model;

public record RepairRequest(int requestId, int customerId, Integer technicianId, Integer serviceId,
                            String deviceType, String brandModel, String problemDescription,
                            String priority, String status, String diagnosisNotes,
                            Double estimatedCost, Double finalCost,
                            String dateSubmitted, String dateCompleted,
                            String serviceName, String customerName, String technicianName) {

    // Display format sa ticket number, e.g. TKT-0001
    public String ticketNumber() {
        return String.format("TKT-%04d", requestId);
    }

    @Override
    public String toString() {
        return String.format("%s | %s %s | %s | %s", ticketNumber(), deviceType, brandModel, priority, status);
    }
}