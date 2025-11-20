package com.job.servlet;
 
import com.job.db.DBConnection;
import com.job.BO.User;
 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
 
@WebServlet("/UserApplicationsServlet")
public class UserApplicationsServlet extends HttpServlet {
 
	 @Override
	  protected void doGet(HttpServletRequest request, HttpServletResponse response)
	      throws ServletException, IOException {
	 
	    response.setContentType("application/json; charset=UTF-8");
	    response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
	    response.setHeader("Access-Control-Allow-Credentials", "true");
	 

	    HttpSession session = request.getSession(false);
	    if (session == null) {
	      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	      response.getWriter().print("{\"error\":\"Not logged in\"}");
	      return;
	    }
	 
	    Object u = session.getAttribute("userSession");
	    if (u == null) {
	      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	      response.getWriter().print("{\"error\":\"Not logged in\"}");
	      return;
	    }
	 
	
	    int userId = -1;
	    if (u instanceof User) {
	      userId = ((User) u).getUserId();
	    } else if (u instanceof Integer) {
	      userId = (Integer) u;
	    } else {
	   
	      try {
	        userId = (int) u.getClass().getMethod("getUserId").invoke(u);
	      } catch (Exception ex) {
	        response.getWriter().print("{\"error\":\"Invalid user session type\"}");
	        return;
	      }
	    }
	 
	    if (userId <= 0) {
	      response.getWriter().print("{\"error\":\"Invalid user session\"}");
	      return;
	    }
	 

	    String sql = 
	    	    "SELECT " +
	    	    "    ja.application_id, " +
	    	    "    ja.job_id, " +
	    	    "    j.title, " +
	    	    "    j.company_name, " +
	    	    "    j.location, " +
	    	    "    CONVERT(VARCHAR(19), ja.applied_date, 120) AS applied_date, " +
	    	    "    ja.status, " +
	    	    "    CONVERT(VARCHAR(19), ja.interview_date, 120) AS interview_date " +
	    	    "FROM Job_Application.M_S_Job_Application ja " +
	    	    "JOIN Job_Application.M_D_Job j ON j.job_id = ja.job_id " +
	    	    "WHERE ja.is_active = 1 AND ja.user_id = ? " +
	    	    "ORDER BY ja.applied_date DESC";

	    
	 
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	 
	      ps.setInt(1, userId);
	 
	      try (ResultSet rs = ps.executeQuery()) {
	        StringBuilder json = new StringBuilder();
	        json.append("{\"applications\":[");
	        boolean first = true;
	 
	        while (rs.next()) {
	          if (!first) json.append(',');
	          first = false;
	 
	          json.append("{")
	          .append("\"applicationId\":").append(rs.getInt("application_id")).append(',')
	          .append("\"jobId\":").append(rs.getInt("job_id")).append(',')
	          .append("\"title\":").append(toJson(rs.getString("title"))).append(',')
	          .append("\"companyName\":").append(toJson(rs.getString("company_name"))).append(',')
	          .append("\"location\":").append(toJson(rs.getString("location"))).append(',')
	          .append("\"appliedDate\":").append(toJson(rs.getString("applied_date"))).append(',')
	          .append("\"interviewDate\":").append(toJson(rs.getString("interview_date"))).append(',')
	          .append("\"status\":").append(toJson(rs.getString("status")))
	          .append("}");

	        }
	 
	        json.append("]}");
	        PrintWriter out = response.getWriter();
	        out.print(json.toString());
	        out.flush();
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	      response.getWriter().print("{\"error\":\"Server error\"}");
	    }
	  }
	 
    private String toJson(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

 