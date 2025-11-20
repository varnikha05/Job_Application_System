package com.job.servlet;
 
import com.job.db.DBConnection;
 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
 
@WebServlet("/RecruiterApplicationsServlet")
public class RecruiterApplicationsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        response.setContentType("application/json; charset=UTF-8");

        HttpSession session = request.getSession(false);

   
        if (session == null || !"recruiter".equals(session.getAttribute("role"))) {
            response.sendError(403, "Unauthorized");
            return;
        }

     
        Integer recruiterId = (Integer) session.getAttribute("recruiterId");
        if (recruiterId == null || recruiterId <= 0) {
            response.sendError(403, "Invalid Recruiter Session");
            return;
        }

        System.out.println("RecruiterApplicationsServlet: recruiterId=" + recruiterId);

        String sql =
                "SELECT ja.application_id, ja.job_id, " +
                "       u.full_name AS applicant_name, u.email AS applicant_email, u.skills AS applicant_skills, " +
                "       CASE WHEN ja.resume IS NULL THEN 0 ELSE DATALENGTH(ja.resume) END AS resume_bytes, " +
                "       j.title AS job_title, j.company_name, j.location, " +
                "       CONVERT(VARCHAR(19), ja.applied_date, 120) AS applied_date, " +
                "       ISNULL(CONVERT(VARCHAR(10), ja.interview_date, 120),'') AS interview_date, " +
                "       ISNULL(ja.final_result,'') AS final_result, " +
                "       ja.status " +
                "FROM Job_Application.M_S_Job_Application ja " +
                "JOIN Job_Application.M_D_Job j ON j.job_id = ja.job_id " +
                "JOIN Job_Application.M_D_User u ON u.user_id = ja.user_id " +
                "WHERE ja.is_active = 1 AND j.recruiter_id = ? " +
                "ORDER BY ja.applied_date DESC";


        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recruiterId);
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder json = new StringBuilder();
                json.append("{\"applications\":[");
                boolean first = true;
                int appCount = 0;
                while (rs.next()) {
                    appCount++;
                    if (!first) json.append(',');
                    first = false;
                    json.append("{")
                        .append("\"applicationId\":").append(rs.getInt("application_id")).append(',')
                        .append("\"jobId\":").append(rs.getInt("job_id")).append(',')
                        .append("\"jobTitle\":").append(toJson(rs.getString("job_title"))).append(',')
                        .append("\"companyName\":").append(toJson(rs.getString("company_name"))).append(',')
                        .append("\"location\":").append(toJson(rs.getString("location"))).append(',')
                        .append("\"applicantName\":").append(toJson(rs.getString("applicant_name"))).append(',')
                        .append("\"applicantEmail\":").append(toJson(rs.getString("applicant_email"))).append(',')
                        .append("\"applicantSkills\":").append(toJson(rs.getString("applicant_skills"))).append(',')
                        .append("\"appliedDate\":").append(toJson(rs.getString("applied_date"))).append(',')
                        .append("\"interviewDate\":").append(toJson(rs.getString("interview_date"))).append(',')
                        .append("\"finalResult\":").append(toJson(rs.getString("final_result"))).append(',')
                        .append("\"status\":").append(toJson(rs.getString("status"))).append(',')
                        .append("\"hasResume\":").append(rs.getInt("resume_bytes") > 0 ? "true" : "false")
                        .append("}");
                }
                System.out.println("RecruiterApplicationsServlet: applications returned=" + appCount);
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

 