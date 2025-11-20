package com.job.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.job.db.DBConnection;

/**
 * ExcelExportServlet - Handles Excel export for admin data
 * Creates real Excel files that users can download
 */
@WebServlet("/ExcelExportServlet")
public class ExcelExportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ExcelExportServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String dataType = request.getParameter("dataType");
        
        try {
            if ("users".equals(dataType)) {
                exportUsersData(request, response);
            } else if ("recruiters".equals(dataType)) {
                exportRecruitersData(request, response);
            } else if ("applications".equals(dataType)) {
                exportApplicationsData(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid data type");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error exporting data", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Export failed");
        }
    }
    
    /**
     * Export users data to CSV (simple format)
     */
    private void exportUsersData(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        Connection conn = DBConnection.getConnection();
        
        // Set response headers for file download
        String filename = "users_export_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        OutputStream out = response.getOutputStream();
        
        try {
        
            String header = "User ID,Full Name,Email,Phone,Gender,Qualification,Skills,Registration Date,Total Applications\n";
            out.write(header.getBytes());
            
          
            String sql = "SELECT u.user_id, u.full_name, u.email, u.phone, u.gender, u.qualification, " +
                        "u.skills, u.created_date, " +
                        "(SELECT COUNT(*) FROM Job_Application.M_S_Job_Application a " +
                        "WHERE a.user_id = u.user_id AND a.is_active = 1) as total_applications " +
                        "FROM Job_Application.M_D_User u WHERE u.is_active = 1 ORDER BY u.created_date DESC";
            
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    row.append(rs.getInt("user_id")).append(",");
                    row.append("\"").append(escapeCSV(rs.getString("full_name"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("email"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("phone"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("gender"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("qualification"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("skills"))).append("\",");
                    row.append("\"").append(rs.getTimestamp("created_date")).append("\",");
                    row.append(rs.getInt("total_applications"));
                    row.append("\n");
                    
                    out.write(row.toString().getBytes());
                }
            }
            
            LOGGER.info("Users data exported successfully");
            
        } finally {
            conn.close();
            out.flush();
            out.close();
        }
    }
    
    /**
     * Export recruiters data to CSV
     */
    private void exportRecruitersData(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        Connection conn = DBConnection.getConnection();
        
    
        String filename = "recruiters_export_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        PrintWriter out = response.getWriter();
        
        try {
      
            String header = "Recruiter ID,Company Name,Recruiter Name,Email,Phone,Status,Registration Date\n";
            out.print(header);
            
        
            String sql = 
            	    "SELECT r.recruiter_id, r.company_name, r.recruiter_name, r.email, r.phone, " +
            	    "r.role, r.created_date " +
            	    "FROM Job_Application.M_D_Recruiter r " +
            	    "WHERE r.is_active = 1 ORDER BY r.created_date DESC";

            
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    row.append(rs.getInt("recruiter_id")).append(",");
                    row.append("\"").append(escapeCSV(rs.getString("company_name"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("recruiter_name"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("email"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("phone"))).append("\",");
               
                    row.append("\"").append(escapeCSV(rs.getString("role"))).append("\",");

                   
                    row.append("\"").append(rs.getTimestamp("created_date")).append("\"");
                    row.append("\n");
                    
                    out.println(row.toString());

                }
            }
            
            LOGGER.info("Recruiters data exported successfully");
            
        } finally {
            conn.close();
            out.flush();
            out.close();
        }
    }
    
    /**
     * Export applications data to CSV
     */
    private void exportApplicationsData(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        Connection conn = DBConnection.getConnection();
        
        // Set response headers
        String filename = "applications_export_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        OutputStream out = response.getOutputStream();
        
        try {

            String header = "Application ID,Job Title,Company,Applicant Name,Applicant Email,Recruiter,Applied Date,Status\n";
            out.write(header.getBytes());
            
       
            String sql = "SELECT a.application_id, j.title as job_title, r.company_name, " +
                        "u.full_name as applicant_name, u.email as applicant_email, " +
                        "r.recruiter_name, a.applied_date, a.status " +
                        "FROM Job_Application.M_S_Job_Application a " +
                        "JOIN Job_Application.M_D_Job j ON a.job_id = j.job_id " +
                        "JOIN Job_Application.M_D_User u ON a.user_id = u.user_id " +
                        "JOIN Job_Application.M_D_Recruiter r ON j.recruiter_id = r.recruiter_id " +
                        "WHERE a.is_active = 1 ORDER BY a.applied_date DESC";
            
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    row.append(rs.getInt("application_id")).append(",");
                    row.append("\"").append(escapeCSV(rs.getString("job_title"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("company_name"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("applicant_name"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("applicant_email"))).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("recruiter_name"))).append("\",");
                    row.append("\"").append(rs.getTimestamp("applied_date")).append("\",");
                    row.append("\"").append(escapeCSV(rs.getString("status"))).append("\"");
                    row.append("\n");
                    
                    out.write(row.toString().getBytes());
                }
            }
            
            LOGGER.info("Applications data exported successfully");
            
        } finally {
            conn.close();
            out.flush();
            out.close();
        }
    }
    
    /**
     * Helper method to escape CSV values
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\""); // Escape quotes in CSV
    }
}