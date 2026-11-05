package com.scc.dao;

import com.scc.Model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public boolean addLog(Integer userId, Integer requestId, String action, String oldValue, String newValue) {
        String sql = "INSERT INTO activity_logs(user_id, request_id, action, old_value, new_value) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userId == null) pstmt.setNull(1, Types.INTEGER);
            else pstmt.setInt(1, userId);

            if (requestId == null) pstmt.setNull(2, Types.INTEGER);
            else pstmt.setInt(2, requestId);

            pstmt.setString(3, action);
            pstmt.setString(4, oldValue);
            pstmt.setString(5, newValue);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Failed to add log: " + e.getMessage());
            return false;
        }
    }

    public boolean addLog(Integer userId, String action) {
        return addLog(userId, null, action, null, null);
    }

    public List<ActivityLog> getAllLogs() {
        return fetchLogs("SELECT * FROM activity_logs ORDER BY log_id DESC", null);
    }

    public List<ActivityLog> getLogsByUser(int userId) {
        return fetchLogs("SELECT * FROM activity_logs WHERE user_id = ? ORDER BY log_id DESC", userId);
    }

    public List<ActivityLog> getLogsByRequest(int requestId) {
        return fetchLogs("SELECT * FROM activity_logs WHERE request_id = ? ORDER BY log_id DESC", requestId);
    }

    private List<ActivityLog> fetchLogs(String sql, Integer param) {
        List<ActivityLog> logList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (param != null) pstmt.setInt(1, param);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    Integer userId = rs.wasNull() ? null : uid;
                    int rid = rs.getInt("request_id");
                    Integer requestId = rs.wasNull() ? null : rid;

                    logList.add(new ActivityLog(
                            rs.getInt("log_id"),
                            userId,
                            requestId,
                            rs.getString("action"),
                            rs.getString("old_value"),
                            rs.getString("new_value"),
                            rs.getString("log_timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
        return logList;
    }
}