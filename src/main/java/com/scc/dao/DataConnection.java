package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataConnection {
    public static  final String DB_URL ="jdbc:sqlite:ComputerRepairServiceRequestSystem.db";

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(DB_URL);
    }
}
