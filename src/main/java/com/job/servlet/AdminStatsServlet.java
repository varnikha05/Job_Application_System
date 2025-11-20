package com.job.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.job.db.DBConnection;
import com.google.gson.JsonObject;

/**
 * AdminStatsServlet - Provides dashboard statistics
 * Endpoint: /AdminStatsServlet
 * Returns JSON with total counts for users, recruiters, jobs, applications
 */
@WebServlet("/AdminStatsServlet")
public class AdminStatsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        JsonObject statsJson = new JsonObject();
        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            
            
            int totalUsers = getTotalUsers(conn);
            int totalRecruiters = getTotalRecruiters(conn);
            int totalJobs = getTotalJobs(conn);
            int totalApplications = getTotalApplications(conn);
            int pendingRecruiters = getPendingRecruiters(conn);
            int jobsToday = getJobsToday(conn);
            int applicationsToday = getApplicationsToday(conn);
            int activeSessions = getActiveSessions(conn);
            
            statsJson.addProperty("totalUsers", totalUsers);
            statsJson.addProperty("totalRecruiters", totalRecruiters);
            statsJson.addProperty("totalJobs", totalJobs);
            statsJson.addProperty("totalApplications", totalApplications);
            statsJson.addProperty("pendingRecruiters", pendingRecruiters);
            statsJson.addProperty("jobsToday", jobsToday);
            statsJson.addProperty("applicationsToday", applicationsToday);
            statsJson.addProperty("activeSessions", activeSessions);
            statsJson.addProperty("status", "success");
            
        } catch (Exception e) {
            e.printStackTrace();
            statsJson = new JsonObject();
            statsJson.addProperty("status", "error");
            statsJson.addProperty("message", "Failed to fetch statistics: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        out.print(statsJson.toString());
        out.flush();
    }
    
    private int getTotalUsers(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Job_Application.M_D_User WHERE is_active = 1";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private int getTotalRecruiters(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Job_Application.M_D_Recruiter WHERE is_active = 1";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private int getTotalJobs(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Job_Application.M_D_Job WHERE is_active = 1";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private int getTotalApplications(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Job_Application.M_S_Job_Application WHERE is_active = 1";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private int getPendingRecruiters(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Job_Application.M_D_Recruiter WHERE role = 'Pending' AND is_active = 1";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private int getJobsToday(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Job_Application.M_D_Job WHERE CAST(created_date AS DATE) = CAST(GETDATE() AS DATE) AND is_active = 1";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private int getApplicationsToday(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM Job_Application.M_S_Job_Application WHERE CAST(applied_date AS DATE) = CAST(GETDATE() AS DATE) AND is_active = 1";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private int getActiveSessions(Connection conn) throws SQLException {
     
    	String sql = "SELECT COUNT(*) as count FROM Job_Application.M_D_User WHERE last_updated >= DATEADD(minute, -10, GETDATE()) AND is_active = 1";

        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
}