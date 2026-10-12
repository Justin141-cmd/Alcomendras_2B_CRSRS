package com.scc.dao;

import com.scc.Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public boolean registerUser(String fullname, String contact, String username, String password, String role) {
        String sql = "INSERT INTO users(fullname, contact, username, password, role, status) VALUES(?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DataConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullname);
            pstmt.setString(2, contact);
            pstmt.setString(3, username);
            pstmt.setString(4, password);
            pstmt.setString(5, role.toUpperCase());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DataConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    // Mu-return og User kung sakto ang username ug password, kung dili kay null
    public User loginUser(String username, String password) {
        String sql = "Select * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DataConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("fullname"),
                            rs.getString("contact"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("status"),
                            rs.getString("date_created"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
        }
        return null;
    }
}