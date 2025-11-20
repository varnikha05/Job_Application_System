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
 * AdminRecruitersServlet - Handles recruiter management operations with job statistics
 * Uses SQL Server schema with JOINs to show recruiter data with job counts
 */
@WebServlet("/AdminRecruitersServlet")
public class AdminRecruitersServlet extends HttpServlet {
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
            query.append("SELECT r.recruiter_id, r.recruiter_name, r.company_name, r.email, r.phone, ");
            query.append("r.address, r.status, r.created_date, r.last_updated, ");
            query.append("(SELECT COUNT(*) FROM Job_Application.M_D_Job j WHERE j.recruiter_id = r.recruiter_id AND j.is_active = 1) as total_jobs, ");
            query.append("(SELECT COUNT(*) FROM Job_Application.M_D_Job j WHERE j.recruiter_id = r.recruiter_id AND j.status = 'Active' AND j.is_active = 1) as active_jobs, ");
            query.append("(SELECT COUNT(*) FROM Job_Application.M_D_Job j WHERE j.recruiter_id = r.recruiter_id AND j.status = 'Closed' AND j.is_active = 1) as closed_jobs ");
            query.append("FROM Job_Application.M_D_Recruiter r ");
            query.append("WHERE r.is_active = 1 ");
            
       
            String search = request.getParameter("search");
            String status = request.getParameter("status");
            String company = request.getParameter("company");
            
            List<Object> parameters = new ArrayList<>();
            
            if (search != null && !search.trim().isEmpty()) {
                query.append("AND (r.recruiter_name LIKE ? OR r.email LIKE ?) ");
                String searchPattern = "%" + search + "%";
                parameters.add(searchPattern);
                parameters.add(searchPattern);
            }
            
            if (status != null && !status.trim().isEmpty()) {
                query.append("AND r.status = ? ");
                parameters.add(status);
            }
            
            if (company != null && !company.trim().isEmpty()) {
                query.append("AND r.company_name LIKE ? ");
                parameters.add("%" + company + "%");
            }
            
            query.append("ORDER BY r.created_date DESC");
            
            ps = conn.prepareStatement(query.toString());
            
       
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            
            rs = ps.executeQuery();
            
            StringBuilder recruitersJson = new StringBuilder();
            recruitersJson.append("[");
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) {
                    recruitersJson.append(",");
                }
                first = false;
                count++;
                
                recruitersJson.append("{");
                recruitersJson.append("\"recruiter_id\":").append(rs.getInt("recruiter_id")).append(",");
                recruitersJson.append("\"recruiter_name\":\"").append(escapeJson(rs.getString("recruiter_name"))).append("\",");
                recruitersJson.append("\"company_name\":\"").append(escapeJson(rs.getString("company_name"))).append("\",");
                recruitersJson.append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",");
                recruitersJson.append("\"phone\":\"").append(escapeJson(rs.getString("phone"))).append("\",");
                recruitersJson.append("\"address\":\"").append(escapeJson(rs.getString("address"))).append("\",");
                recruitersJson.append("\"status\":\"").append(escapeJson(rs.getString("status"))).append("\",");
                recruitersJson.append("\"created_date\":\"").append(rs.getTimestamp("created_date").toString()).append("\",");
                recruitersJson.append("\"last_updated\":\"").append(rs.getTimestamp("last_updated").toString()).append("\",");
                recruitersJson.append("\"total_jobs\":").append(rs.getInt("total_jobs")).append(",");
                recruitersJson.append("\"active_jobs\":").append(rs.getInt("active_jobs")).append(",");
                recruitersJson.append("\"closed_jobs\":").append(rs.getInt("closed_jobs"));
                recruitersJson.append("}");
            }
            recruitersJson.append("]");
            
            StringBuilder result = new StringBuilder();
            result.append("{");
            result.append("\"status\":\"success\",");
            result.append("\"data\":").append(recruitersJson.toString()).append(",");
            result.append("\"recordsTotal\":").append(count).append(",");
            result.append("\"recordsFiltered\":").append(count);
            result.append("}");
            
            out.print(result.toString());
            
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
            addRecruiter(request, response, out);
        } else if ("update".equals(action)) {
            updateRecruiter(request, response, out);
        } else if ("delete".equals(action)) {
            deleteRecruiter(request, response, out);
        } else if ("approve".equals(action)) {
            approveRecruiter(request, response, out);
        } else if ("reject".equals(action)) {
            rejectRecruiter(request, response, out);
        } else {
            StringBuilder error = new StringBuilder();
            error.append("{");
            error.append("\"status\":\"error\",");
            error.append("\"message\":\"Invalid action\"");
            error.append("}");
            out.print(error.toString());
        }
    }
    
    private void addRecruiter(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "INSERT INTO Job_Application.M_D_Recruiter " +
                        "(recruiter_name, company_name, email, password, phone, address, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("recruiter_name"));
            ps.setString(2, request.getParameter("company_name"));
            ps.setString(3, request.getParameter("email"));
            ps.setString(4, request.getParameter("password")); // Should be encrypted in real app
            ps.setString(5, request.getParameter("phone"));
            ps.setString(6, request.getParameter("address"));
            ps.setString(7, "Pending"); // Default status for new recruiters
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Recruiter added successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to add recruiter\"");
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
    
    private void updateRecruiter(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE Job_Application.M_D_Recruiter SET " +
                        "recruiter_name = ?, company_name = ?, email = ?, phone = ?, " +
                        "address = ?, status = ?, last_updated = GETDATE() " +
                        "WHERE recruiter_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("recruiter_name"));
            ps.setString(2, request.getParameter("company_name"));
            ps.setString(3, request.getParameter("email"));
            ps.setString(4, request.getParameter("phone"));
            ps.setString(5, request.getParameter("address"));
            ps.setString(6, request.getParameter("status"));
            ps.setInt(7, Integer.parseInt(request.getParameter("recruiter_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Recruiter updated successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to update recruiter or recruiter not found\"");
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
    
    private void deleteRecruiter(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Soft delete
            String sql = "UPDATE Job_Application.M_D_Recruiter SET is_active = 0, last_updated = GETDATE() " +
                        "WHERE recruiter_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(request.getParameter("recruiter_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Recruiter deleted successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to delete recruiter or recruiter not found\"");
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
    
    private void approveRecruiter(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE Job_Application.M_D_Recruiter SET status = 'Approved', last_updated = GETDATE() " +
                        "WHERE recruiter_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(request.getParameter("recruiter_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Recruiter approved successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to approve recruiter or recruiter not found\"");
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
    
    private void rejectRecruiter(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE Job_Application.M_D_Recruiter SET status = 'Rejected', last_updated = GETDATE() " +
                        "WHERE recruiter_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(request.getParameter("recruiter_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"Recruiter rejected successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to reject recruiter or recruiter not found\"");
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