package com.job.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.job.BO.User;
import com.job.db.DBConnection;

/**
 * Servlet implementation class UpdateUserProfileServlet
 */
@WebServlet("/UserProfileUpdateServlet")
@MultipartConfig(maxFileSize = 1024 * 1024 * 5) // 5MB max file size
public class UserProfileUpdateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserProfileUpdateServlet() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) 
        		session.getAttribute("userSession") : null;
 
        if (loggedUser == null) {
            response.getWriter().write("{\"ok\":false, \"msg\":\"Not logged in\"}");
            return;
        }
 
        int userId = loggedUser.getUserId();
        
       
        String phone = request.getParameter("phone");
        String skills = request.getParameter("skills");
        

        if (phone == null || phone.trim().isEmpty()) {
            response.getWriter().write("{\"ok\":false, \"msg\":\"Phone number is required\"}");
            return;
        }
        
        if (skills == null || skills.trim().isEmpty()) {
            response.getWriter().write("{\"ok\":false, \"msg\":\"Skills are required\"}");
            return;
        }
    
        phone = phone.trim().replaceAll("[^0-9]", "");
        if (phone.length() != 10 || !phone.matches("^[6-9][0-9]{9}$")) {
            response.getWriter().write("{\"ok\":false, \"msg\":\"Invalid mobile number. Please enter 10-digit number starting with 6-9\"}");
            return;
        }
        
        skills = skills.trim();
 
        Part resumePart = null;
        try {
            resumePart = request.getPart("resume");
        } catch (Exception e) {
         
        }
 
        try (Connection conn = DBConnection.getConnection()) {
 
            String sql;
            PreparedStatement ps;
 
            if (resumePart != null && resumePart.getSize() > 0) {
              
                if (resumePart.getSize() > 5 * 1024 * 1024) { // 5MB
                    response.getWriter().write("{\"ok\":false, \"msg\":\"Resume file too large. Max 5MB allowed.\"}");
                    return;
                }
                
                String contentType = resumePart.getContentType();
                if (contentType == null || (!contentType.contains("pdf") && 
                    !contentType.contains("msword") && 
                    !contentType.contains("document"))) {
                    response.getWriter().write("{\"ok\":false, \"msg\":\"Invalid file type. Only PDF and DOC files allowed.\"}");
                    return;
                }
                
                sql = "UPDATE Job_Application.M_D_User SET phone = ?, skills = ?, resume = ? WHERE user_id = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, phone);
                ps.setString(2, skills);
                ps.setBinaryStream(3, resumePart.getInputStream());
                ps.setInt(4, userId);
            } else {
                sql = "UPDATE Job_Application.M_D_User SET phone = ?, skills = ? WHERE user_id = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, phone);
                ps.setString(2, skills);
                ps.setInt(3, userId);
            }
            
            int rowsUpdated = ps.executeUpdate();
            ps.close();
            
            if (rowsUpdated > 0) {
            
                loggedUser.setPhone(phone);
                loggedUser.setSkills(skills);
                session.setAttribute("userSession", loggedUser);
                
                response.getWriter().write("{\"ok\":true, \"msg\":\"Profile updated successfully\"}");
            } else {
                response.getWriter().write("{\"ok\":false, \"msg\":\"No changes made\"}");
            }
 
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"ok\":false, \"msg\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
}