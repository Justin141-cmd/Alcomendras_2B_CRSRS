package com.scc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseInitializer {

    public static void initializer() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. users
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        user_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                        fullname     TEXT NOT NULL,
                        contact      TEXT NOT NULL,
                        username     TEXT NOT NULL UNIQUE,
                        password     TEXT NOT NULL,
                        role         TEXT NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER', 'TECHNICIAN')),
                        status       TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE')),
                        date_created TEXT DEFAULT (datetime('now', 'localtime'))
                    )
                    """);

            // 2. service_types
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS service_types (
                        service_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                        service_name   TEXT NOT NULL UNIQUE,
                        description    TEXT,
                        base_price     REAL NOT NULL DEFAULT 0,
                        estimated_days INTEGER NOT NULL DEFAULT 1,
                        is_active      INTEGER NOT NULL DEFAULT 1
                    )
                    """);

            // 3. repair_requests
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS repair_requests (
                        request_id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id         INTEGER NOT NULL,
                        technician_id       INTEGER,
                        service_id          INTEGER,
                        device_type         TEXT NOT NULL,
                        brand_model         TEXT NOT NULL,
                        problem_description TEXT NOT NULL,
                        priority            TEXT NOT NULL DEFAULT 'NORMAL',
                        status              TEXT NOT NULL DEFAULT 'PENDING',
                        diagnosis_notes     TEXT,
                        estimated_cost      REAL,
                        final_cost          REAL,
                        date_submitted      TEXT DEFAULT (datetime('now', 'localtime')),
                        date_completed      TEXT,
                        FOREIGN KEY (customer_id)   REFERENCES users(user_id),
                        FOREIGN KEY (technician_id) REFERENCES users(user_id),
                        FOREIGN KEY (service_id)    REFERENCES service_types(service_id)
                    )
                    """);

            // 4. activity_logs (AUDIT LOG)
            // Kung karaan pa ang structure sa table, i-replace sa bag-o
            replaceOldActivityLogs(conn);

            // user_id nullable: para sa FAILED_LOGIN ug USER_REGISTERED nga wala pay user_id
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id     INTEGER,
                        action_type TEXT NOT NULL,
                        description TEXT NOT NULL,
                        timestamp   TEXT DEFAULT (datetime('now', 'localtime')),
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
                    )
                    """);

            seedDefaultAdmin(conn);

            System.out.println("\n -> Database tables Verified. ");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    // Kung naay karaan nga activity_logs nga kulang sa gikinahanglan nga columns, i-drop aron mahimo ang bag-o
    private static void replaceOldActivityLogs(Connection conn) throws SQLException {
        Set<String> columns = new HashSet<>();

        try (PreparedStatement pstmt = conn.prepareStatement("PRAGMA table_info(activity_logs)");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase());
            }
        }

        if (columns.isEmpty()) {
            return;   // wala pa ang table, ang CREATE TABLE na ang mohimo
        }

        boolean valid = columns.containsAll(List.of("id", "user_id", "action_type", "description", "timestamp"));

        if (!valid) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE activity_logs");
            }
            System.out.println(" -> Old activity_logs table replaced with the new audit log structure.");
        }
    }

    // Kung wala pay users, maghimo og default admin aron naay maka-login ug maka-approve
    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        boolean empty;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = pstmt.executeQuery()) {
            empty = rs.next() && rs.getInt(1) == 0;
        }

        if (!empty) {
            return;
        }

        String sql = "INSERT INTO users(fullname, contact, username, password, role, status) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "System Admin");
            pstmt.setString(2, "09000000000");
            pstmt.setString(3, "admin");
            pstmt.setString(4, "admin123");
            pstmt.setString(5, "ADMIN");
            pstmt.setString(6, "ACTIVE");
            pstmt.executeUpdate();
            System.out.println(" -> Default admin created (username: admin / password: admin123)");
        }
    }
}