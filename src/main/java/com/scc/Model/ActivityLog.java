package com.scc.Model;

public record ActivityLog(int id, Integer userId, String actionType,
                          String description, String timestamp) {

    @Override
    public String toString() {
        return String.format("[%d] %s | User: %s | %s | %s",
                id,
                timestamp,
                userId == null ? "N/A" : userId,
                actionType,
                description);
    }
}