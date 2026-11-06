package com.scc.dao;

import com.scc.Model.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // Main nga method sa audit log: naay user (admin, customer, technician)
    public boolean logAction(int userId, String actionType, String description) {
        return insertLog(userId, actionType, description);
    }

    // Para sa events nga wala pay user_id (failed login, register)
    public boolean logSystemAction(String actionType, String description) {
        return insertLog(null, actionType, description);
    }

    private boolean insertLog(Integer userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs(user_id, action_type, description) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (userId == null) pstmt.setNull(1, Types.INTEGER);
            else pstmt.setInt(1, userId);

            pstmt.setString(2, actionType);
            pstmt.setString(3, description);
            pstmt.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Failed to add log: " + e.getMessage());
            return false;
        }
    }

    public List<ActivityLog> getAllLogs() {
        return fetchLogs("SELECT * FROM activity_logs ORDER BY timestamp DESC, id DESC", null);
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        return fetchLogs("SELECT * FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC", userId);
    }

    // Mo-return sa gidaghanon nga na-delete, o -1 kung nag-fail
    public int clearAllLogs() {
        String sql = "DELETE FROM activity_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to clear logs: " + e.getMessage());
            return -1;
        }
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

                    logList.add(new ActivityLog(
                            rs.getInt("id"),
                            userId,
                            rs.getString("action_type"),
                            rs.getString("description"),
                            rs.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
        return logList;
    }
}