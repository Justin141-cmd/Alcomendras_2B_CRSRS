package com.scc.dao;

import com.scc.Model.RepairRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepairRequestDao {

    // Gigamit sa tanan nga SELECT: naay JOIN para sa service, customer, ug technician name
    private static final String BASE_SELECT = """
            SELECT r.*, s.service_name, c.fullname AS customer_name, t.fullname AS technician_name
            FROM repair_requests r
            LEFT JOIN service_types s ON r.service_id = s.service_id
            JOIN users c ON r.customer_id = c.user_id
            LEFT JOIN users t ON r.technician_id = t.user_id
            """;

    // Mo-return sa bag-ong request_id (ticket number), o -1 kung nag-fail
    public int submitRequest(int customerId, int serviceId, String deviceType, String brandModel,
                             String problemDescription, String priority) {
        // Ang estimated_cost gikuha sa base_price sa napilian nga service
        String sql = """
                INSERT INTO repair_requests(customer_id, service_id, device_type, brand_model,
                                            problem_description, priority, estimated_cost)
                VALUES(?, ?, ?, ?, ?, ?, (SELECT base_price FROM service_types WHERE service_id = ?))
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, serviceId);
            pstmt.setString(3, deviceType);
            pstmt.setString(4, brandModel);
            pstmt.setString(5, problemDescription);
            pstmt.setString(6, priority.toUpperCase());
            pstmt.setInt(7, serviceId);
            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to submit request: " + e.getMessage());
        }
        return -1;
    }

    // Jobs nga gi-assign sa technician. Active una, urgent una, completed/cancelled sa ubos.
    public List<RepairRequest> getRequestsByTechnician(int technicianId) {
        return fetchRequests(BASE_SELECT + """
             WHERE r.technician_id = ?
             ORDER BY CASE WHEN r.status IN ('COMPLETED', 'CANCELLED') THEN 1 ELSE 0 END,
                      CASE r.priority WHEN 'URGENT' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'NORMAL' THEN 2 ELSE 3 END,
                      r.request_id
             """, technicianId);
    }

    // Ang oldStatus check mopugong kung giusab na sa uban ang ticket. Iya ra nga job ang pwede.
    public boolean updateStatus(int requestId, int technicianId, String oldStatus, String newStatus) {
        String sql = newStatus.equals("COMPLETED")
                ? "UPDATE repair_requests SET status = ?, date_completed = datetime('now', 'localtime') " +
                "WHERE request_id = ? AND technician_id = ? AND status = ?"
                : "UPDATE repair_requests SET status = ? " +
                "WHERE request_id = ? AND technician_id = ? AND status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, requestId);
            pstmt.setInt(3, technicianId);
            pstmt.setString(4, oldStatus);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update status: " + e.getMessage());
            return false;
        }
    }

    public boolean updateDiagnosisAndCost(int requestId, int technicianId, String notes,
                                          Double estimatedCost, Double finalCost) {
        String sql = "UPDATE repair_requests SET diagnosis_notes = ?, estimated_cost = ?, final_cost = ? " +
                "WHERE request_id = ? AND technician_id = ? AND status NOT IN ('COMPLETED', 'CANCELLED')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notes);

            if (estimatedCost == null) pstmt.setNull(2, Types.REAL);
            else pstmt.setDouble(2, estimatedCost);

            if (finalCost == null) pstmt.setNull(3, Types.REAL);
            else pstmt.setDouble(3, finalCost);

            pstmt.setInt(4, requestId);
            pstmt.setInt(5, technicianId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update diagnosis/cost: " + e.getMessage());
            return false;
        }
    }

    public List<RepairRequest> getAllRequests() {
        return fetchRequests(BASE_SELECT + " ORDER BY r.request_id DESC", null);
    }

    public List<RepairRequest> getRequestsByCustomer(int customerId) {
        return fetchRequests(BASE_SELECT + " WHERE r.customer_id = ? ORDER BY r.request_id DESC", customerId);
    }

    public RepairRequest getRequestById(int requestId) {
        List<RepairRequest> list = fetchRequests(BASE_SELECT + " WHERE r.request_id = ?", requestId);
        return list.isEmpty() ? null : list.get(0);
    }

    // Soft delete: status = CANCELLED, pending ra ug iya ra nga request ang pwede
    public boolean cancelRequest(int requestId, int customerId) {
        String sql = "UPDATE repair_requests SET status = 'CANCELLED' " +
                "WHERE request_id = ? AND customer_id = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, requestId);
            pstmt.setInt(2, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to cancel request: " + e.getMessage());
            return false;
        }
    }

    private List<RepairRequest> fetchRequests(String sql, Integer param) {
        List<RepairRequest> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (param != null) pstmt.setInt(1, param);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching requests: " + e.getMessage());
        }
        return list;
    }

    private RepairRequest mapRow(ResultSet rs) throws SQLException {
        return new RepairRequest(
                rs.getInt("request_id"),
                rs.getInt("customer_id"),
                getNullableInt(rs, "technician_id"),
                getNullableInt(rs, "service_id"),
                rs.getString("device_type"),
                rs.getString("brand_model"),
                rs.getString("problem_description"),
                rs.getString("priority"),
                rs.getString("status"),
                rs.getString("diagnosis_notes"),
                getNullableDouble(rs, "estimated_cost"),
                getNullableDouble(rs, "final_cost"),
                rs.getString("date_submitted"),
                rs.getString("date_completed"),
                rs.getString("service_name"),
                rs.getString("customer_name"),
                rs.getString("technician_name")
        );
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    // Search by ticket number: gamita ang getRequestById() nga naa na

    public List<RepairRequest> searchByCustomerName(String name) {
        return fetchRequestsByText(BASE_SELECT + " WHERE c.fullname LIKE ? ORDER BY r.request_id DESC",
                "%" + name + "%");
    }

    public List<RepairRequest> getRequestsByStatus(String status) {
        return fetchRequestsByText(BASE_SELECT + " WHERE r.status = ? ORDER BY r.request_id DESC",
                status.toUpperCase());
    }

    // date format: YYYY-MM-DD
    public List<RepairRequest> getRequestsByDate(String date) {
        return fetchRequestsByText(BASE_SELECT + " WHERE DATE(r.date_submitted) = ? ORDER BY r.request_id DESC",
                date);
    }

    private List<RepairRequest> fetchRequestsByText(String sql, String param) {
        List<RepairRequest> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, param);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching requests: " + e.getMessage());
        }
        return list;
    }

    public List<RepairRequest> getAssignableRequests() {
        return fetchRequests(BASE_SELECT +
                " WHERE r.status NOT IN ('COMPLETED', 'CANCELLED')" +
                " ORDER BY (r.technician_id IS NOT NULL), r.request_id DESC", null);
    }

    public boolean assignTechnician(int requestId, int technicianId) {
        String sql = "UPDATE repair_requests SET technician_id = ? " +
                "WHERE request_id = ? AND status NOT IN ('COMPLETED', 'CANCELLED')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, technicianId);
            pstmt.setInt(2, requestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to assign technician: " + e.getMessage());
            return false;
        }
    }
}