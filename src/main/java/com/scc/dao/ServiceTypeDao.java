package com.scc.dao;

import com.scc.Model.ServiceType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTypeDao {

    public boolean addService(String name, String description, double basePrice, int estimatedDays) {
        String sql = "INSERT INTO service_types(service_name, description, base_price, estimated_days) VALUES(?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, basePrice);
            pstmt.setInt(4, estimatedDays);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to add service: " + e.getMessage());
            return false;
        }
    }

    // excludeId = ID nga dili iapil sa check (para sa update). Gamita ang 0 kung add.
    public boolean serviceNameExists(String name, int excludeId) {
        String sql = "SELECT 1 FROM service_types WHERE LOWER(service_name) = LOWER(?) AND service_id <> ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, excludeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    public List<ServiceType> getAllServices() {
        return fetchServices("SELECT * FROM service_types ORDER BY service_id");
    }

    // Mao ni ang makita sa customer inig submit og request
    public List<ServiceType> getActiveServices() {
        return fetchServices("SELECT * FROM service_types WHERE is_active = 1 ORDER BY service_id");
    }

    public ServiceType getServiceById(int serviceId) {
        String sql = "SELECT * FROM service_types WHERE service_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, serviceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching service: " + e.getMessage());
        }
        return null;
    }

    public boolean updateService(int serviceId, String name, String description, double basePrice, int estimatedDays) {
        String sql = "UPDATE service_types SET service_name = ?, description = ?, base_price = ?, estimated_days = ? WHERE service_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setDouble(3, basePrice);
            pstmt.setInt(4, estimatedDays);
            pstmt.setInt(5, serviceId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to update service: " + e.getMessage());
            return false;
        }
    }

    // Soft delete: active = false para i-archive, true para i-restore
    public boolean setActive(int serviceId, boolean active) {
        String sql = "UPDATE service_types SET is_active = ? WHERE service_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, active ? 1 : 0);
            pstmt.setInt(2, serviceId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to change service status: " + e.getMessage());
            return false;
        }
    }

    private List<ServiceType> fetchServices(String sql) {
        List<ServiceType> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching services: " + e.getMessage());
        }
        return list;
    }

    private ServiceType mapRow(ResultSet rs) throws SQLException {
        return new ServiceType(
                rs.getInt("service_id"),
                rs.getString("service_name"),
                rs.getString("description"),
                rs.getDouble("base_price"),
                rs.getInt("estimated_days"),
                rs.getInt("is_active") == 1
        );
    }
}