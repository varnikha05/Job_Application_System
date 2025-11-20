package com.job.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.job.db.DBConnection;

/**
 * RecentActivityServlet - Gets real recent activity from database
 * Beginner level code that is easy to understand
 */
@WebServlet("/RecentActivityServlet")
public class RecentActivityServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
  
    private static final Logger LOGGER = Logger.getLogger(RecentActivityServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
 
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
        
            Connection conn = DBConnection.getConnection();
            
          
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{\"status\":\"success\",\"activities\":[");
            
          
            boolean hasActivity = false;
    
            hasActivity = addRecentUsers(conn, jsonResponse, hasActivity);
            
     
            hasActivity = addRecentJobs(conn, jsonResponse, hasActivity);

            hasActivity = addRecentApplications(conn, jsonResponse, hasActivity);
            
          
            hasActivity = addRecentRecruiters(conn, jsonResponse, hasActivity);
            
            jsonResponse.append("]}");
            
            out.print(jsonResponse.toString());
     
            LOGGER.info("Recent activity loaded successfully");
            
          
            conn.close();
            
        } catch (Exception e) {
         
            LOGGER.log(Level.SEVERE, "Error loading recent activity", e);
            
  
            out.print("{\"status\":\"error\",\"message\":\"Failed to load recent activity\"}");
        }
    }
    
    /**
     * Add recent user registrations to activity list
     */
    private boolean addRecentUsers(Connection conn, StringBuilder json, boolean hasActivity) throws SQLException {
        String sql = "SELECT TOP 3 full_name, created_date FROM Job_Application.M_D_User " +
                    "WHERE is_active = 1 ORDER BY created_date DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                if (hasActivity) {
                    json.append(",");
                }
                hasActivity = true;
                
                String timeAgo = getTimeAgo(rs.getTimestamp("created_date").getTime());
                String userName = rs.getString("full_name");
                
                json.append("{");
                json.append("\"action\":\"New user registered: ").append(escapeJson(userName)).append("\",");
                json.append("\"time\":\"").append(timeAgo).append("\",");
                json.append("\"icon\":\"icon-user\",");
                json.append("\"color\":\"success\"");
                json.append("}");
            }
        }
        
        return hasActivity;
    }
    
    /**
     * Add recent job postings to activity list
     */
    private boolean addRecentJobs(Connection conn, StringBuilder json, boolean hasActivity) throws SQLException {
        String sql = "SELECT TOP 3 j.title, j.posted_date, r.company_name " +
                    "FROM Job_Application.M_D_Job j " +
                    "LEFT JOIN Job_Application.M_D_Recruiter r ON j.recruiter_id = r.recruiter_id " +
                    "WHERE j.is_active = 1 ORDER BY j.posted_date DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                if (hasActivity) {
                    json.append(",");
                }
                hasActivity = true;
                
                String timeAgo = getTimeAgo(rs.getTimestamp("posted_date").getTime());
                String jobTitle = rs.getString("title");
                String companyName = rs.getString("company_name");
                
                json.append("{");
                json.append("\"action\":\"New job posted: ").append(escapeJson(jobTitle));
                if (companyName != null) {
                    json.append(" by ").append(escapeJson(companyName));
                }
                json.append("\",");
                json.append("\"time\":\"").append(timeAgo).append("\",");
                json.append("\"icon\":\"icon-briefcase\",");
                json.append("\"color\":\"info\"");
                json.append("}");
            }
        }
        
        return hasActivity;
    }
    
    /**
     * Add recent applications to activity list
     */
    private boolean addRecentApplications(Connection conn, StringBuilder json, boolean hasActivity) throws SQLException {
        String sql = "SELECT TOP 3 u.full_name, j.title, a.applied_date " +
                    "FROM Job_Application.M_S_Job_Application a " +
                    "JOIN Job_Application.M_D_User u ON a.user_id = u.user_id " +
                    "JOIN Job_Application.M_D_Job j ON a.job_id = j.job_id " +
                    "WHERE a.is_active = 1 ORDER BY a.applied_date DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                if (hasActivity) {
                    json.append(",");
                }
                hasActivity = true;
                
                String timeAgo = getTimeAgo(rs.getTimestamp("applied_date").getTime());
                String userName = rs.getString("full_name");
                String jobTitle = rs.getString("title");
                
                json.append("{");
                json.append("\"action\":\"Application: ").append(escapeJson(userName))
                    .append(" applied for ").append(escapeJson(jobTitle)).append("\",");
                json.append("\"time\":\"").append(timeAgo).append("\",");
                json.append("\"icon\":\"icon-send\",");
                json.append("\"color\":\"warning\"");
                json.append("}");
            }
        }
        
        return hasActivity;
    }
    
    /**
     * Add recent recruiter registrations to activity list
     */
    private boolean addRecentRecruiters(Connection conn, StringBuilder json, boolean hasActivity) throws SQLException {
        String sql = "SELECT TOP 2 company_name, created_date FROM Job_Application.M_D_Recruiter " +
                    "WHERE is_active = 1 ORDER BY created_date DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                if (hasActivity) {
                    json.append(",");
                }
                hasActivity = true;
                
                String timeAgo = getTimeAgo(rs.getTimestamp("created_date").getTime());
                String companyName = rs.getString("company_name");
                
                json.append("{");
                json.append("\"action\":\"New recruiter joined: ").append(escapeJson(companyName)).append("\",");
                json.append("\"time\":\"").append(timeAgo).append("\",");
                json.append("\"icon\":\"icon-check\",");
                json.append("\"color\":\"success\"");
                json.append("}");
            }
        }
        
        return hasActivity;
    }
    
    /**
     * 
     */
    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        // Convert to minutes
        long minutes = diff / (1000 * 60);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (minutes < 1440) { // < 24 hours
            long hours = minutes / 60;
            return hours + " hours ago";
        } else {
            long days = minutes / 1440;
            return days + " days ago";
        }
    }
    
    /**
     * 
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}