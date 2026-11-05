package com.scc.Model;

public record ActivityLog(int logId, Integer userId, Integer requestId,
                          String action, String oldValue, String newValue,
                          String logTimestamp) {

    @Override
    public String toString() {
        return String.format("[%d] %s | User: %s | Request: %s | %s | %s -> %s",
                logId,
                logTimestamp,
                userId == null ? "N/A" : userId,
                requestId == null ? "N/A" : requestId,
                action,
                oldValue == null ? "-" : oldValue,
                newValue == null ? "-" : newValue);
    }
}