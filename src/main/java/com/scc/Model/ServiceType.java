package com.scc.Model;

public record ServiceType(int serviceId, String serviceName, String description,
                          double basePrice, int estimatedDays, boolean isActive) {

    @Override
    public String toString() {
        return String.format("[%d] %s - PHP %.2f (%d day/s) - %s",
                serviceId, serviceName, basePrice, estimatedDays,
                isActive ? "ACTIVE" : "ARCHIVED");
    }
}