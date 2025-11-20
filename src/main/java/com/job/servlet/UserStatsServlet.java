package com.job.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.job.BO.User;
import com.job.db.DBConnection;

/**
 * UserStatsServlet - Provides real user statistics from database
 */
@WebServlet("/UserStatsServlet")
public class UserStatsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        User loggedUser = (session != null) ? (User) session.getAttribute("userSession") : null;
        
        if (loggedUser == null) {
            out.print("{\"error\":\"Not logged in\"}");
            return;
        }
        
        int userId = loggedUser.getUserId();
        
        try (Connection conn = DBConnection.getConnection()) {
            
          
            String sql1 = "SELECT COUNT(*) as total FROM Job_Application.M_S_Job_Application WHERE user_id = ? AND is_active = 1";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setInt(1, userId);
            ResultSet rs1 = ps1.executeQuery();
            int totalApplications = 0;
            if (rs1.next()) {
                totalApplications = rs1.getInt("total");
            }
            rs1.close();
            ps1.close();
            
           
            String sql2 = "SELECT COUNT(*) as pending FROM Job_Application.M_S_Job_Application WHERE user_id = ? AND status = 'Applied' AND is_active = 1";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, userId);
            ResultSet rs2 = ps2.executeQuery();
            int pendingApplications = 0;
            if (rs2.next()) {
                pendingApplications = rs2.getInt("pending");
            }
            rs2.close();
            ps2.close();
      
            String sql3 = "SELECT COUNT(*) as selected FROM Job_Application.M_S_Job_Application WHERE user_id = ? AND status = 'Selected' AND is_active = 1";
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            ps3.setInt(1, userId);
            ResultSet rs3 = ps3.executeQuery();
            int selectedApplications = 0;
            if (rs3.next()) {
                selectedApplications = rs3.getInt("selected");
            }
            rs3.close();
            ps3.close();
            
          
            String sql4 = "SELECT COUNT(*) as interviews FROM Job_Application.M_S_Job_Application WHERE user_id = ? AND interview_date IS NOT NULL AND is_active = 1";
            PreparedStatement ps4 = conn.prepareStatement(sql4);
            ps4.setInt(1, userId);
            ResultSet rs4 = ps4.executeQuery();
            int scheduledInterviews = 0;
            if (rs4.next()) {
                scheduledInterviews = rs4.getInt("interviews");
            }
            rs4.close();
            ps4.close();
            
       
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"totalApplications\":").append(totalApplications).append(",");
            json.append("\"pendingApplications\":").append(pendingApplications).append(",");
            json.append("\"selectedApplications\":").append(selectedApplications).append(",");
            json.append("\"scheduledInterviews\":").append(scheduledInterviews);
            json.append("}");
            
            out.print(json.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"error\":\"Failed to load statistics\"}");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}