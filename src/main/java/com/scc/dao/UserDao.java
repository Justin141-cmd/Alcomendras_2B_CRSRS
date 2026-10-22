package com.scc.dao;

import com.scc.Model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean registerUser(String fullname, String contact, String username, String password, String role) {
        String sql = "INSERT INTO users(fullname, contact, username, password, role, status) VALUES(?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
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

        try (Connection conn = DatabaseConnection.getConnection();
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    if("PENDING".equalsIgnoreCase(status)){
                        System.out.println("-> Account is pending, Waiting for Approval");
                        return null;
                    }
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("fullname"),
                            rs.getString("contact"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("status"),
                            rs.getString("date_created"));
                }else {
                    System.out.println(" -> Invalid username or password");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUser(){
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            while (rs.next()) {
                userList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("fullname"),
                        rs.getString("contact"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getString("date_created")
                ));
            }
        }catch (SQLException e){
            System.err.println("Error Fetching all users: "+ e.getMessage());
        }
        return userList;
    }

    public List<User> getPendingUser(){
        List<User> pendingList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'PENDING'";

        try(Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            while (rs.next()) {
                pendingList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("fullname"),
                        rs.getString("contact"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getString("date_created")
                ));
            }
        }catch (SQLException e){
            System.err.println("Error Fetching all users: "+ e.getMessage());
        }
        return pendingList;
    }

    public boolean approvedUser(int userId){
        String sql = "UPDATE users SET status = 'ACTIVE' WHERE user_id = ?";

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, userId);

            int rowsUpdated = pstmt.executeUpdate();

            if(rowsUpdated > 0){
                System.out.println("\n -> User ID: "+userId+" Approved Successfully" );
                return true;
            }else {
                System.out.println("\n -> User ID not found!");
            }
        }catch (SQLException e){
            System.err.println("Error fetching users" + e.getMessage());
        }
        return false;
    }
}