package com.job.db;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
public class DBConnection {
	private static final String URL = "jdbc:sqlserver://192.168.3.125:1433;database=SQLTraining;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "DzTrainee";      
    private static final String PASSWORD = "Sql!2025";






    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println(" SQL Server JDBC Driver Loaded Successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading SQL Server JDBC Driver!");
            e.printStackTrace();
        }
    }
 
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
 
    // Quick test
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null)
                System.out.println("Connected successfully!");
            else
                System.out.println("Connection failed!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}