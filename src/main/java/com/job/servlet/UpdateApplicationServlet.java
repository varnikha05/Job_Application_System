package com.job.servlet;

import com.job.db.DBConnection;
 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class UpdateApplicationServlet
 */
@WebServlet("/UpdateApplicationServlet")
public class UpdateApplicationServlet extends HttpServlet {
  
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateApplicationServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	 
	    response.setContentType("application/json; charset=UTF-8");
	 
	    HttpSession session = request.getSession(false);
	    Integer recruiterId = (session == null) ? null : (Integer) session.getAttribute("recruiterId");
	    if (recruiterId == null || recruiterId <= 0) {
	        response.getWriter().print("{\"ok\":false,\"error\":\"Not logged in\"}");
	        return;
	    }
	 
	    String appIdStr     = request.getParameter("applicationId");
	    String status       = request.getParameter("status");
	    String interviewStr = request.getParameter("interviewDate");
	    String finalResult  = request.getParameter("finalResult"); 
	 
	    if (appIdStr == null || appIdStr.isEmpty()) {
	        response.getWriter().print("{\"ok\":false,\"error\":\"Missing applicationId\"}");
	        return;
	    }
	 
	    int applicationId = Integer.parseInt(appIdStr);
	 
	   
	    Timestamp interviewTs = null;
	    if (interviewStr != null && !interviewStr.trim().isEmpty()) {
	        interviewTs = Timestamp.valueOf(interviewStr.trim() + " 00:00:00");
	    }
	 
	    String sql =
	        "UPDATE ja SET " +
	        "    ja.status = ?, " +
	        "    ja.interview_date = ?, " +
	        "    ja.final_result = ?, " +
	        "    ja.last_updated = GETDATE() " +
	        "FROM Job_Application.M_S_Job_Application ja " +
	        "JOIN Job_Application.M_D_Job j ON j.job_id = ja.job_id " +
	        "WHERE ja.application_id = ? AND ja.is_active = 1 AND j.recruiter_id = ?";
	 
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	 
	     
	        ps.setString(1, status);
	 
	      
	        if (interviewTs != null) {
	            ps.setTimestamp(2, interviewTs);
	        } else {
	            ps.setNull(2, Types.TIMESTAMP);
	        }
	 
	 
	        if (finalResult != null && !finalResult.trim().isEmpty()) {
	            ps.setString(3, finalResult.trim());
	        } else {
	            ps.setNull(3, Types.VARCHAR);
	        }
	 
	     
	        ps.setInt(4, applicationId);
	        ps.setInt(5, recruiterId);
	 
	        int rows = ps.executeUpdate();
	        if (rows > 0) {

	           
	            String getJobIdSql = 
	                "SELECT job_id FROM Job_Application.M_S_Job_Application WHERE application_id = ?";
	            
	            Integer jobId = null;
	            try (PreparedStatement psJob = conn.prepareStatement(getJobIdSql)) {
	                psJob.setInt(1, applicationId);
	                ResultSet rs = psJob.executeQuery();
	                if (rs.next()) {
	                    jobId = rs.getInt("job_id");
	                }
	            }

	           
	            if (jobId != null && finalResult != null && !finalResult.trim().isEmpty()) {

	                String newJobStatus = finalResult.equalsIgnoreCase("Selected") 
	                                      ? "Closed"     
	                                      : "Active";   

	                String updateJobSql =
	                	    "UPDATE Job_Application.M_D_Job SET status = ?, last_updated = GETDATE() WHERE job_id = ?";


	                try (PreparedStatement psUpdateJob = conn.prepareStatement(updateJobSql)) {
	                    psUpdateJob.setString(1, newJobStatus);
	                    psUpdateJob.setInt(2, jobId);
	                    psUpdateJob.executeUpdate();
	                }
	            }

	          
	            response.getWriter().print("{\"ok\":true}");
	        

	        } else {
	            response.getWriter().print("{\"ok\":false,\"error\":\"Not allowed or not found\"}");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.getWriter().print("{\"ok\":false,\"error\":\"Server error\"}");
	    }
	}}
	 


