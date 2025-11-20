package com.job.servlet;

import java.io.IOException;
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
 * AdminJobsServlet - Handles job management operations with JOINs
 * Uses SQL Server schema: Job_Application.M_D_Job and Job_Application.M_D_Recruiter
 */
@WebServlet("/AdminJobsServlet")
public class AdminJobsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
       
            StringBuilder query = new StringBuilder();
            query.append("SELECT j.job_id, j.title, j.description, j.location, j.experience, ");
            query.append("j.salary, j.skills, j.status AS status, j.posted_date, j.closing_date, ");

            query.append("r.recruiter_name, r.company_name, r.email as recruiter_email, ");
            query.append("(SELECT COUNT(*) FROM Job_Application.M_S_Job_Application a WHERE a.job_id = j.job_id AND a.is_active = 1) as application_count ");
            query.append("FROM Job_Application.M_D_Job j ");
            query.append("LEFT JOIN Job_Application.M_D_Recruiter r ON j.recruiter_id = r.recruiter_id ");
            query.append("WHERE j.is_active = 1 ");
            
       
            String status = request.getParameter("status");
            String search = request.getParameter("search");
            String location = request.getParameter("location");
            String recruiter = request.getParameter("recruiter");
            
            String dateFrom = request.getParameter("dateFrom");
            String dateTo = request.getParameter("dateTo");
            
            List<Object> parameters = new ArrayList<>();
             
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                query.append("AND CAST(j.posted_date AS DATE) >= ? ");
                parameters.add(dateFrom);
            }
             
            if (dateTo != null && !dateTo.trim().isEmpty()) {
                query.append("AND CAST(j.posted_date AS DATE) <= ? ");
                parameters.add(dateTo);
            }
            
            
            
            if (status != null && !status.trim().isEmpty()) {
                query.append("AND j.status = ? ");
                parameters.add(status);
            }
            
            if (search != null && !search.trim().isEmpty()) {
                query.append("AND (j.title LIKE ? OR j.description LIKE ? OR j.skills LIKE ?) ");
                String searchPattern = "%" + search + "%";
                parameters.add(searchPattern);
                parameters.add(searchPattern);
                parameters.add(searchPattern);
            }
            
            if (location != null && !location.trim().isEmpty()) {
                query.append("AND j.location LIKE ? ");
                parameters.add("%" + location + "%");
            }
            
            if (recruiter != null && !recruiter.trim().isEmpty()) {
                query.append("AND (r.recruiter_name LIKE ? OR r.company_name LIKE ?) ");
                String recruiterPattern = "%" + recruiter + "%";
                parameters.add(recruiterPattern);
                parameters.add(recruiterPattern);
            }
            
            query.append("ORDER BY j.posted_date DESC");
            
            ps = conn.prepareStatement(query.toString());
            
        
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            
            rs = ps.executeQuery();
            
            StringBuilder jobsJson = new StringBuilder();
            jobsJson.append("[");
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) {
                    jobsJson.append(",");
                }
                first = false;
                count++;
                
                jobsJson.append("{");
                jobsJson.append("\"job_id\":").append(rs.getInt("job_id")).append(",");
                jobsJson.append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",");
                jobsJson.append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",");
                jobsJson.append("\"location\":\"").append(escapeJson(rs.getString("location"))).append("\",");
                jobsJson.append("\"experience\":\"").append(escapeJson(rs.getString("experience"))).append("\",");
                jobsJson.append("\"salary\":\"").append(escapeJson(rs.getString("salary"))).append("\",");
                jobsJson.append("\"skills\":\"").append(escapeJson(rs.getString("skills"))).append("\",");
                jobsJson.append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",");
                jobsJson.append("\"posted_date\":\"").append(rs.getTimestamp("posted_date").toString()).append("\",");
                
                String closingDate = rs.getTimestamp("closing_date") != null ? 
                    rs.getTimestamp("closing_date").toString() : "";
                jobsJson.append("\"closing_date\":\"").append(closingDate).append("\",");
                
                jobsJson.append("\"recruiter_name\":\"").append(escapeJson(rs.getString("recruiter_name"))).append("\",");
                jobsJson.append("\"company_name\":\"").append(escapeJson(rs.getString("company_name"))).append("\",");
                jobsJson.append("\"recruiter_email\":\"").append(escapeJson(rs.getString("recruiter_email"))).append("\",");
                jobsJson.append("\"application_count\":").append(rs.getInt("application_count"));
                jobsJson.append("}");
            }
            jobsJson.append("]");
            
         //
            out.print("{\"jobs\":"+jobsJson.toString()+"}");
            
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
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String action = request.getParameter("action");
        
        if ("add".equals(action)) {
            addJob(request, response, out);
        } else if ("update".equals(action)) {
            updateJob(request, response, out);
        } else if ("delete".equals(action)) {
            deleteJob(request, response, out);
        } else if ("updateStatus".equals(action)) {
            updateJobStatus(request, response, out);
        } else {
            StringBuilder error = new StringBuilder();
            error.append("{");
            error.append("\"status\":\"error\",");
            error.append("\"message\":\"Invalid action\"");
            error.append("}");
            out.print(error.toString());
        }
    }
    
    private void updateJobStatus(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "UPDATE Job_Application.M_D_Job SET status = ?, last_updated = GETDATE() " +
                        "WHERE job_id = ? AND is_active = 1";

            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("status"));
            ps.setInt(2, Integer.parseInt(request.getParameter("job_id")));

            int result = ps.executeUpdate();

            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Job status updated successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to update job status or job not found\"");
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

    private void addJob(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "INSERT INTO Job_Application.M_D_Job " +
                        "(recruiter_id, title, description, location, experience, salary, skills, status, closing_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(request.getParameter("recruiter_id")));
            ps.setString(2, request.getParameter("title"));
            ps.setString(3, request.getParameter("description"));
            ps.setString(4, request.getParameter("location"));
            ps.setString(5, request.getParameter("experience"));
            ps.setString(6, request.getParameter("salary"));
            ps.setString(7, request.getParameter("skills"));
            ps.setString(8, request.getParameter("status"));
            
            String closingDate = request.getParameter("closing_date");
            if (closingDate != null && !closingDate.trim().isEmpty()) {
                ps.setString(9, closingDate);
            } else {
                ps.setNull(9, java.sql.Types.TIMESTAMP);
            }
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Job added successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to add job\"");
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
    
    private void updateJob(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE Job_Application.M_D_Job SET " +
                        "title = ?, description = ?, location = ?, experience = ?, " +
                        "salary = ?, skills = ?, status = ?, closing_date = ?, last_updated = GETDATE() " +
                        "WHERE job_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("title"));
            ps.setString(2, request.getParameter("description"));
            ps.setString(3, request.getParameter("location"));
            ps.setString(4, request.getParameter("experience"));
            ps.setString(5, request.getParameter("salary"));
            ps.setString(6, request.getParameter("skills"));
            ps.setString(7, request.getParameter("status"));
            
            String closingDate = request.getParameter("closing_date");
            if (closingDate != null && !closingDate.trim().isEmpty()) {
                ps.setString(8, closingDate);
            } else {
                ps.setNull(8, java.sql.Types.TIMESTAMP);
            }
            
            ps.setInt(9, Integer.parseInt(request.getParameter("job_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Job updated successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to update job or job not found\"");
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
    
    private void deleteJob(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Soft delete
            String sql = "UPDATE Job_Application.M_D_Job SET is_active = 0, last_updated = GETDATE() " +
                        "WHERE job_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(request.getParameter("job_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Job deleted successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to delete job or job not found\"");
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