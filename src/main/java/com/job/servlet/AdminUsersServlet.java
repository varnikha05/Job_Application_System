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
 * AdminUsersServlet - Handles user management operations with application statistics
 * Uses SQL Server schema with JOINs to show user data with application counts
 */
@WebServlet("/AdminUsersServlet")
public class AdminUsersServlet extends HttpServlet {
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
            query.append("SELECT u.user_id, u.full_name, u.email, u.phone, u.gender, ");
            query.append("u.qualification, u.skills, u.resume, u.created_date, u.last_updated, ");
            query.append("(SELECT COUNT(*) FROM Job_Application.M_S_Job_Application a WHERE a.user_id = u.user_id AND a.is_active = 1) as total_applications, ");
            query.append("(SELECT COUNT(*) FROM Job_Application.M_S_Job_Application a WHERE a.user_id = u.user_id AND a.status = 'Applied' AND a.is_active = 1) as pending_applications, ");
            query.append("(SELECT COUNT(*) FROM Job_Application.M_S_Job_Application a WHERE a.user_id = u.user_id AND a.status = 'Selected' AND a.is_active = 1) as selected_applications ");
            query.append("FROM Job_Application.M_D_User u ");
            query.append("WHERE u.is_active = 1 ");
            
            // Add filters
            String search = request.getParameter("search");
            String qualification = request.getParameter("qualification");
            String skills = request.getParameter("skills");
            
            List<Object> parameters = new ArrayList<>();
            
            if (search != null && !search.trim().isEmpty()) {
                query.append("AND (u.full_name LIKE ? OR u.email LIKE ?) ");
                String searchPattern = "%" + search + "%";
                parameters.add(searchPattern);
                parameters.add(searchPattern);
            }
            
            if (qualification != null && !qualification.trim().isEmpty()) {
                query.append("AND u.qualification LIKE ? ");
                parameters.add("%" + qualification + "%");
            }
            
            if (skills != null && !skills.trim().isEmpty()) {
                query.append("AND u.skills LIKE ? ");
                parameters.add("%" + skills + "%");
            }
            
            query.append("ORDER BY u.created_date DESC");
            
            ps = conn.prepareStatement(query.toString());
            
      
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            
            rs = ps.executeQuery();
            
            StringBuilder usersJson = new StringBuilder();
            usersJson.append("[");
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) {
                    usersJson.append(",");
                }
                first = false;
                count++;
                
                usersJson.append("{");
                usersJson.append("\"user_id\":").append(rs.getInt("user_id")).append(",");
                usersJson.append("\"full_name\":\"").append(escapeJson(rs.getString("full_name"))).append("\",");
                usersJson.append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",");
                usersJson.append("\"phone\":\"").append(escapeJson(rs.getString("phone"))).append("\",");
                usersJson.append("\"gender\":\"").append(escapeJson(rs.getString("gender"))).append("\",");
                usersJson.append("\"qualification\":\"").append(escapeJson(rs.getString("qualification"))).append("\",");
                usersJson.append("\"skills\":\"").append(escapeJson(rs.getString("skills"))).append("\",");
                usersJson.append("\"resume\":\"").append(escapeJson(rs.getString("resume"))).append("\",");
                usersJson.append("\"created_date\":\"").append(rs.getTimestamp("created_date").toString()).append("\",");
                usersJson.append("\"last_updated\":\"").append(rs.getTimestamp("last_updated").toString()).append("\",");
                usersJson.append("\"total_applications\":").append(rs.getInt("total_applications")).append(",");
                usersJson.append("\"pending_applications\":").append(rs.getInt("pending_applications")).append(",");
                usersJson.append("\"selected_applications\":").append(rs.getInt("selected_applications"));
                usersJson.append("}");
            }
            usersJson.append("]");
            
            StringBuilder result = new StringBuilder();
            result.append("{");
            result.append("\"status\":\"success\",");
            result.append("\"data\":").append(usersJson.toString()).append(",");
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
            addUser(request, response, out);
        } else if ("update".equals(action)) {
            updateUser(request, response, out);
        } else if ("delete".equals(action)) {
            deleteUser(request, response, out);
        } else {
            StringBuilder error = new StringBuilder();
            error.append("{");
            error.append("\"status\":\"error\",");
            error.append("\"message\":\"Invalid action\"");
            error.append("}");
            out.print(error.toString());
        }
    }
    
    private void addUser(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "INSERT INTO Job_Application.M_D_User " +
                        "(full_name, email, password, phone, gender, qualification, skills, resume) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("full_name"));
            ps.setString(2, request.getParameter("email"));
            ps.setString(3, request.getParameter("password")); 
            ps.setString(4, request.getParameter("phone"));
            ps.setString(5, request.getParameter("gender"));
            ps.setString(6, request.getParameter("qualification"));
            ps.setString(7, request.getParameter("skills"));
            ps.setString(8, request.getParameter("resume"));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"User added successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to add user\"");
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
    
    private void updateUser(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "UPDATE Job_Application.M_D_User SET " +
                        "full_name = ?, email = ?, phone = ?, gender = ?, " +
                        "qualification = ?, skills = ?, resume = ?, last_updated = GETDATE() " +
                        "WHERE user_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setString(1, request.getParameter("full_name"));
            ps.setString(2, request.getParameter("email"));
            ps.setString(3, request.getParameter("phone"));
            ps.setString(4, request.getParameter("gender"));
            ps.setString(5, request.getParameter("qualification"));
            ps.setString(6, request.getParameter("skills"));
            ps.setString(7, request.getParameter("resume"));
            ps.setInt(8, Integer.parseInt(request.getParameter("user_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"User updated successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to update user or user not found\"");
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
    
    private void deleteUser(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Soft delete  
            String sql = "UPDATE Job_Application.M_D_User SET is_active = 0, last_updated = GETDATE() " +
                        "WHERE user_id = ? AND is_active = 1";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(request.getParameter("user_id")));
            
            int result = ps.executeUpdate();
            
            StringBuilder response_json = new StringBuilder();
            response_json.append("{");
            if (result > 0) {
                response_json.append("\"status\":\"success\",");
                response_json.append("\"message\":\"User deleted successfully\"");
            } else {
                response_json.append("\"status\":\"error\",");
                response_json.append("\"message\":\"Failed to delete user or user not found\"");
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