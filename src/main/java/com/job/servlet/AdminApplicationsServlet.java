package com.job.servlet;

import com.job.db.DBConnection;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
 
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.job.db.DBConnection;

/**
 * AdminApplicationsServlet - Handles application management operations with comprehensive JOINs
 * Uses SQL Server schema with multiple JOINs to show complete application data
 */
@WebServlet("/AdminApplicationsServlet")
public class AdminApplicationsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
 
        resp.setContentType("application/json; charset=UTF-8");
 
        String period = req.getParameter("period"); // today|week|month|year
        if (period == null || period.isEmpty()) period = "week";
 
        JsonObject out = new JsonObject();
        JsonArray trend = new JsonArray();
        JsonArray rows  = new JsonArray();
 
        try (Connection conn = DBConnection.getConnection()) {
 
            String trendSql;
            
            // For "today", show only hours with applications
            if ("today".equals(period)) {
                trendSql = 
                    "SELECT " +
                    "  CONVERT(VARCHAR(16), applied_date, 120) AS day, " +
                    "  COUNT(application_id) AS cnt " +
                    "FROM Job_Application.M_S_Job_Application " +
                    "WHERE CAST(applied_date AS DATE) = CAST(GETDATE() AS DATE) " +
                    "  AND is_active = 1 " +
                    "GROUP BY CONVERT(VARCHAR(16), applied_date, 120) " +
                    "ORDER BY CONVERT(VARCHAR(16), applied_date, 120) ASC";
            } else {
                // For week, month, year - show daily breakdown
                int daysBack;
                switch(period) {
                    case "week":
                        daysBack = 6; // Last 7 days (including today)
                        break;
                    case "month":
                        daysBack = 29; // Last 30 days (including today)
                        break;
                    case "year":
                        daysBack = 364; // Last 365 days (including today)
                        break;
                    default:
                        daysBack = 6;
                }
                
                trendSql =
                    "WITH d AS ( " +
                    "  SELECT CAST(GETDATE() AS DATE) AS d " +
                    "  UNION ALL " +
                    "  SELECT DATEADD(DAY,-1,d) FROM d WHERE d > DATEADD(DAY, -" + daysBack + ", CAST(GETDATE() AS DATE)) " +
                    ") " +
                    "SELECT d.d AS day, COUNT(a.application_id) AS cnt " +
                    "FROM d LEFT JOIN Job_Application.M_S_Job_Application a " +
                    "  ON CAST(a.applied_date AS DATE) = d.d AND a.is_active = 1 " +
                    "GROUP BY d.d ORDER BY d.d ASC OPTION (MAXRECURSION 366)";
            }
            try (PreparedStatement ps = conn.prepareStatement(trendSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject p = new JsonObject();
                    p.addProperty("date", rs.getString("day"));
                    p.addProperty("count", rs.getInt("cnt"));
                    trend.add(p);
                }
            }
 

            String listSql =
                    "SELECT TOP 200 a.application_id, " +
                    "       j.title AS job_title, " +
                    "       u.full_name AS applicant_name, " +
                    "       r.recruiter_name, " +
                    "       CONVERT(VARCHAR(19), a.applied_date, 120) AS applied_date, " +
                    "       a.status " +
                    "FROM Job_Application.M_S_Job_Application a " +
                    "JOIN Job_Application.M_D_Job j ON j.job_id = a.job_id " +
                    "JOIN Job_Application.M_D_User u ON u.user_id = a.user_id " +
                    "JOIN Job_Application.M_D_Recruiter r ON r.recruiter_id = j.recruiter_id " +
                    "WHERE a.is_active = 1 " +
                    "ORDER BY a.applied_date DESC";
            try (PreparedStatement ps = conn.prepareStatement(listSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject row = new JsonObject();
                    row.addProperty("applicationId", rs.getInt("application_id"));
                    row.addProperty("jobTitle", rs.getString("job_title"));
                    row.addProperty("applicantName", rs.getString("applicant_name"));
                    row.addProperty("recruiterName", rs.getString("recruiter_name"));
                    row.addProperty("appliedDate", rs.getString("applied_date"));
                    row.addProperty("status", rs.getString("status"));
                    rows.add(row);
                }
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        out.add("trend", trend);
        out.add("applications", rows);
        resp.getWriter().print(out.toString());
    }

    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String action = request.getParameter("action");
        
        if ("update_status".equals(action)) {
            updateApplicationStatus(request, response, out);
        } else if ("schedule_interview".equals(action)) {
            scheduleInterview(request, response, out);
        } else if ("add_remarks".equals(action)) {
            addRemarks(request, response, out);
        } else if ("delete".equals(action)) {
            deleteApplication(request, response, out);
        } else {
            StringBuilder error = new StringBuilder();
            error.append("{");
            error.append("\"status\":\"error\",");
            error.append("\"message\":\"Invalid action\"");
            error.append("}");
            out.print(error.toString());
        }
    }
    
    private void updateApplicationStatus(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE Job_Application.M_S_Job_Application SET " +
                        "status = ?, remarks = ?, last_updated = GETDATE() " +
                        "WHERE application_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("status"));
            ps.setString(2, request.getParameter("remarks"));
            ps.setInt(3, Integer.parseInt(request.getParameter("application_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Application status updated successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to update application status or application not found\"");
            }
            response_json.append("}");
            
            out.print(response_json.toString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            StringBuilder error = new StringBuilder();
            error.append("{");
            error.append("\"status\":\"error\",");
            error.append("\"message\":\"Database error: ").append(escapeJson(e.getMessage())).append("\"");
            error.append("}");
            out.print(error.toString());
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void scheduleInterview(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE Job_Application.M_S_Job_Application SET " +
                        "interview_date = ?, status = 'Interview Scheduled', " +
                        "remarks = ?, last_updated = GETDATE() " +
                        "WHERE application_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("interview_date"));
            ps.setString(2, request.getParameter("remarks"));
            ps.setInt(3, Integer.parseInt(request.getParameter("application_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Interview scheduled successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to schedule interview or application not found\"");
            }
            response_json.append("}");
            
            out.print(response_json.toString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            StringBuilder error = new StringBuilder();
            error.append("{");
            error.append("\"status\":\"error\",");
            error.append("\"message\":\"Database error: ").append(escapeJson(e.getMessage())).append("\"");
            error.append("}");
            out.print(error.toString());
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void addRemarks(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE Job_Application.M_S_Job_Application SET " +
                        "remarks = ?, last_updated = GETDATE() " +
                        "WHERE application_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("remarks"));
            ps.setInt(2, Integer.parseInt(request.getParameter("application_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Remarks added successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to add remarks or application not found\"");
            }
            response_json.append("}");
            
            out.print(response_json.toString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            StringBuilder error = new StringBuilder();
            error.append("{");
            error.append("\"status\":\"error\",");
            error.append("\"message\":\"Database error: ").append(escapeJson(e.getMessage())).append("\"");
            error.append("}");
            out.print(error.toString());
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void deleteApplication(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Soft delet
            String sql = "UPDATE Job_Application.M_S_Job_Application SET is_active = 0, last_updated = GETDATE() " +
                        "WHERE application_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(request.getParameter("application_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Application deleted successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to delete application or application not found\"");
            }
            response_json.append("}");
            
            out.print(response_json.toString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            StringBuilder error = new StringBuilder();
            error.append("{");
            error.append("\"status\":\"error\",");
            error.append("\"message\":\"Database error: ").append(escapeJson(e.getMessage())).append("\"");
            error.append("}");
            out.print(error.toString());
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
  
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\b", "\\b")
                 .replace("\f", "\\f")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}