package com.job.servlet;



import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.job.BO.User;
import com.job.db.DBConnection;


@WebServlet("/UserManagementServlet")
public class UserManagementServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UserManagementServlet.class.getName());
    
    // SQL Queries
    private static final String SELECT_ALL_USERS = 
        "SELECT u.user_id, u.full_name, u.email, u.phone, u.gender, u.qualification, " +
        "u.skills, u.created_date, u.last_updated, u.is_active, " +
        "(SELECT COUNT(*) FROM Job_Application.M_S_Job_Application a WHERE a.user_id = u.user_id AND a.is_active = 1) as total_applications, " +
        "(SELECT COUNT(*) FROM Job_Application.M_S_Job_Application a WHERE a.user_id = u.user_id AND a.status = 'Applied' AND a.is_active = 1) as pending_applications " +
        "FROM Job_Application.M_D_User u WHERE u.is_active = 1 ORDER BY u.created_date DESC";
    
    private static final String SELECT_USER_BY_ID = 
        "SELECT * FROM Job_Application.M_D_User WHERE user_id = ? AND is_active = 1";
    
    private static final String UPDATE_USER = 
        "UPDATE Job_Application.M_D_User SET full_name = ?, email = ?, phone = ?, gender = ?, " +
        "qualification = ?, skills = ?, last_updated = GETDATE() WHERE user_id = ?";
    
    private static final String SOFT_DELETE_USER = 
        "UPDATE Job_Application.M_D_User SET is_active = 0, last_updated = GETDATE() WHERE user_id = ?";
    
    private static final String CHECK_EMAIL_EXISTS = 
        "SELECT COUNT(*) FROM Job_Application.M_D_User WHERE email = ? AND user_id != ? AND is_active = 1";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String action = request.getParameter("action");
        
        try {
            if ("getUser".equals(action)) {
                handleGetUser(request, response);
            } else {
                handleGetAllUsers(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in UserManagementServlet GET", e);
            sendErrorResponse(out, "Error fetching user data: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        try {
            if ("update".equals(action)) {
                handleUpdateUser(request, response);
            } else if ("delete".equals(action)) {
                handleDeleteUser(request, response);
            } else {
                sendErrorResponse(response.getWriter(), "Invalid action specified");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in UserManagementServlet POST", e);
            sendErrorResponse(response.getWriter(), "Error processing request: " + e.getMessage());
        }
    }
    
    /**
     * Handle fetching all users with statistics
     */
    private void handleGetAllUsers(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        PrintWriter out = response.getWriter();
        
        // Get filter parameters
        String search = request.getParameter("search");
        String qualification = request.getParameter("qualification");
        String gender = request.getParameter("gender");
        String skillFilter = request.getParameter("skills");
        
        StringBuilder query = new StringBuilder(SELECT_ALL_USERS);
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        
       
        if (search != null && !search.trim().isEmpty()) {
            conditions.add("(u.full_name LIKE ? OR u.email LIKE ? OR u.phone LIKE ?)");
            String searchParam = "%" + search.trim() + "%";
            parameters.add(searchParam);
            parameters.add(searchParam);
            parameters.add(searchParam);
        }
        
        if (qualification != null && !qualification.trim().isEmpty()) {
            conditions.add("u.qualification LIKE ?");
            parameters.add("%" + qualification.trim() + "%");
        }
        
        if (gender != null && !gender.trim().isEmpty()) {
            conditions.add("u.gender = ?");
            parameters.add(gender);
        }
        
        if (skillFilter != null && !skillFilter.trim().isEmpty()) {
            conditions.add("u.skills LIKE ?");
            parameters.add("%" + skillFilter.trim() + "%");
        }
        
        
        if (!conditions.isEmpty()) {
          
            String baseQuery = query.toString().replace("WHERE u.is_active = 1", 
                "WHERE u.is_active = 1 AND " + String.join(" AND ", conditions));
            query = new StringBuilder(baseQuery);
        }
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            
        
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            jsonResponse.append("\"status\":\"success\",");
            jsonResponse.append("\"users\":[");
            
            boolean first = true;
            int count = 0;
            
            while (rs.next()) {
                if (!first) {
                    jsonResponse.append(",");
                }
                first = false;
                count++;
                
                jsonResponse.append("{");
                jsonResponse.append("\"userId\":").append(rs.getInt("user_id")).append(",");
                jsonResponse.append("\"fullName\":\"").append(escapeJson(rs.getString("full_name"))).append("\",");
                jsonResponse.append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",");
                jsonResponse.append("\"phone\":\"").append(escapeJson(rs.getString("phone"))).append("\",");
                jsonResponse.append("\"gender\":\"").append(escapeJson(rs.getString("gender"))).append("\",");
                jsonResponse.append("\"qualification\":\"").append(escapeJson(rs.getString("qualification"))).append("\",");
                jsonResponse.append("\"skills\":\"").append(escapeJson(rs.getString("skills"))).append("\",");
                jsonResponse.append("\"createdDate\":\"").append(rs.getTimestamp("created_date").toString()).append("\",");
                jsonResponse.append("\"lastUpdated\":\"").append(rs.getTimestamp("last_updated").toString()).append("\",");
                jsonResponse.append("\"totalApplications\":").append(rs.getInt("total_applications")).append(",");
                jsonResponse.append("\"pendingApplications\":").append(rs.getInt("pending_applications")).append(",");
                jsonResponse.append("\"status\":\"").append(rs.getBoolean("is_active") ? "Active" : "Inactive").append("\"");
                jsonResponse.append("}");
            }
            
            jsonResponse.append("],");
            jsonResponse.append("\"totalRecords\":").append(count);
            jsonResponse.append("}");
            
            out.print(jsonResponse.toString());
            LOGGER.info("Retrieved " + count + " users with filters applied");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching users", e);
            sendErrorResponse(out, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * Handle fetching single user by ID
     */
    private void handleGetUser(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        PrintWriter out = response.getWriter();
        String userIdStr = request.getParameter("userId");
        
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            sendErrorResponse(out, "User ID is required");
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SELECT_USER_BY_ID)) {
                
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    StringBuilder jsonResponse = new StringBuilder();
                    jsonResponse.append("{");
                    jsonResponse.append("\"status\":\"success\",");
                    jsonResponse.append("\"user\":{");
                    jsonResponse.append("\"userId\":").append(rs.getInt("user_id")).append(",");
                    jsonResponse.append("\"fullName\":\"").append(escapeJson(rs.getString("full_name"))).append("\",");
                    jsonResponse.append("\"email\":\"").append(escapeJson(rs.getString("email"))).append("\",");
                    jsonResponse.append("\"phone\":\"").append(escapeJson(rs.getString("phone"))).append("\",");
                    jsonResponse.append("\"gender\":\"").append(escapeJson(rs.getString("gender"))).append("\",");
                    jsonResponse.append("\"qualification\":\"").append(escapeJson(rs.getString("qualification"))).append("\",");
                    jsonResponse.append("\"skills\":\"").append(escapeJson(rs.getString("skills"))).append("\",");
                    jsonResponse.append("\"createdDate\":\"").append(rs.getTimestamp("created_date").toString()).append("\",");
                    jsonResponse.append("\"lastUpdated\":\"").append(rs.getTimestamp("last_updated").toString()).append("\"");
                    jsonResponse.append("}}");
                    
                    out.print(jsonResponse.toString());
                    LOGGER.info("Retrieved user details for ID: " + userId);
                } else {
                    sendErrorResponse(out, "User not found");
                }
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(out, "Invalid user ID format");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while fetching user", e);
            sendErrorResponse(out, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * Handle updating user details
     */
    private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        PrintWriter out = response.getWriter();
        
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String fullName = request.getParameter("fullName");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String gender = request.getParameter("gender");
            String qualification = request.getParameter("qualification");
            String skills = request.getParameter("skills");
            
          
            if (fullName == null || fullName.trim().isEmpty()) {
                sendErrorResponse(out, "Full name is required");
                return;
            }
            
            if (email == null || email.trim().isEmpty()) {
                sendErrorResponse(out, "Email is required");
                return;
            }
            
           
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement checkPs = conn.prepareStatement(CHECK_EMAIL_EXISTS)) {
                
                checkPs.setString(1, email);
                checkPs.setInt(2, userId);
                ResultSet rs = checkPs.executeQuery();
                
                if (rs.next() && rs.getInt(1) > 0) {
                    sendErrorResponse(out, "Email already exists for another user");
                    return;
                }
            }
            
      
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_USER)) {
                
                ps.setString(1, fullName.trim());
                ps.setString(2, email.trim());
                ps.setString(3, phone != null ? phone.trim() : "");
                ps.setString(4, gender != null ? gender.trim() : "");
                ps.setString(5, qualification != null ? qualification.trim() : "");
                ps.setString(6, skills != null ? skills.trim() : "");
                ps.setInt(7, userId);
                
                int rowsUpdated = ps.executeUpdate();
                
                if (rowsUpdated > 0) {
                    out.print("{\"status\":\"success\",\"message\":\"User updated successfully\"}");
                    LOGGER.info("User updated successfully - ID: " + userId + ", Name: " + fullName);
                } else {
                    sendErrorResponse(out, "No user found with the given ID");
                }
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(out, "Invalid user ID format");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating user", e);
            sendErrorResponse(out, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * Handle soft deleting user (set is_active = 0)
     */
    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        PrintWriter out = response.getWriter();
        String userIdStr = request.getParameter("userId");
        
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            sendErrorResponse(out, "User ID is required");
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SOFT_DELETE_USER)) {
                
                ps.setInt(1, userId);
                int rowsDeleted = ps.executeUpdate();
                
                if (rowsDeleted > 0) {
                    out.print("{\"status\":\"success\",\"message\":\"User deleted successfully\"}");
                    LOGGER.warning("User soft deleted - ID: " + userId);
                } else {
                    sendErrorResponse(out, "No user found with the given ID");
                }
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(out, "Invalid user ID format");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while deleting user", e);
            sendErrorResponse(out, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * Utility method to send error responses
     */
    private void sendErrorResponse(PrintWriter out, String message) {
        out.print("{\"status\":\"error\",\"message\":\"" + escapeJson(message) + "\"}");
    }
    
    /**
     * Utility method to escape JSON strings
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}