package com.job.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.job.db.DBConnection;

@WebServlet("/RecruiterStatsServlet")
public class RecruiterStatsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("recruiterId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\":\"Unauthorized access\"}");
            out.flush();
            return;
        }

        int recruiterId = (Integer) session.getAttribute("recruiterId");
        System.out.println("Recruiter session ID: " + recruiterId);

        try (Connection conn = DBConnection.getConnection()) {
            Map<String, Object> stats = getRecruiterStats(conn, recruiterId);
            
            @SuppressWarnings("unchecked")
            Map<String, Integer> statusDist = (Map<String, Integer>) stats.get("jobStatusDistribution");

         
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"totalJobs\":").append(stats.get("totalJobs")).append(",");
            json.append("\"totalApplications\":").append(stats.get("totalApplications")).append(",");
            json.append("\"activeJobs\":").append(statusDist.get("active")).append(",");

            json.append("\"todayApplications\":").append(stats.get("todayApplications")).append(",");
            json.append("\"avgApplications\":").append(stats.get("avgApplications")).append(",");
            json.append("\"selectionRate\":").append(stats.get("selectionRate")).append(",");
            json.append("\"avgTimeToHire\":").append(stats.get("avgTimeToHire")).append(",");
            json.append("\"topSkill\":\"").append(escape(stats.get("topSkill"))).append("\",");

     
            json.append("\"monthlyApplications\":[");
            int[] monthlyApps = (int[]) stats.get("monthlyApplications");
            for (int i = 0; i < monthlyApps.length; i++) {
                json.append(monthlyApps[i]);
                if (i < monthlyApps.length - 1) json.append(",");
            }
            json.append("],");

        
           
            json.append("\"jobStatusDistribution\":{");
            json.append("\"active\":").append(statusDist.get("active")).append(",");
            json.append("\"closed\":").append(statusDist.get("closed")).append(",");
            json.append("\"draft\":").append(statusDist.get("draft"));
            json.append("}");

            json.append("}");
            out.print(json.toString());

            System.out.println("Recruiter stats fetched successfully for ID: " + recruiterId);

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    private Map<String, Object> getRecruiterStats(Connection conn, int recruiterId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();

   
        String totalJobsQuery = "SELECT COUNT(*) as total FROM Job_Application.M_D_Job WHERE recruiter_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(totalJobsQuery)) {
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            stats.put("totalJobs", rs.next() ? rs.getInt("total") : 0);
        }

    
        String totalAppsQuery =
                "SELECT COUNT(*) as total " +
                "FROM Job_Application.M_S_Job_Application ja " +
                "JOIN Job_Application.M_D_Job j ON ja.job_id = j.job_id " +
                "WHERE j.recruiter_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(totalAppsQuery)) {
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            stats.put("totalApplications", rs.next() ? rs.getInt("total") : 0);
        }

   

   
        String todayAppsQuery =
                "SELECT COUNT(*) as today " +
                "FROM Job_Application.M_S_Job_Application ja " +
                "JOIN Job_Application.M_D_Job j ON ja.job_id = j.job_id " +
                "WHERE j.recruiter_id = ? AND CAST(ja.applied_date AS DATE) = CAST(GETDATE() AS DATE)";
        try (PreparedStatement ps = conn.prepareStatement(todayAppsQuery)) {
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            stats.put("todayApplications", rs.next() ? rs.getInt("today") : 0);
        }

    
        int totalJobs = (Integer) stats.get("totalJobs");
        int totalApps = (Integer) stats.get("totalApplications");
        double avgApplications = totalJobs > 0 ? (double) totalApps / totalJobs : 0.0;
        stats.put("avgApplications", Math.round(avgApplications * 10.0) / 10.0);

 
        String selectedQuery =
                "SELECT COUNT(*) as selected " +
                "FROM Job_Application.M_S_Job_Application ja " +
                "JOIN Job_Application.M_D_Job j ON ja.job_id = j.job_id " +
                "WHERE j.recruiter_id = ? AND ja.status = 'Selected'";
        int selectedCount = 0;
        try (PreparedStatement ps = conn.prepareStatement(selectedQuery)) {
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            selectedCount = rs.next() ? rs.getInt("selected") : 0;
        }
        double selectionRate = totalApps > 0 ? (double) selectedCount * 100 / totalApps : 0.0;
        stats.put("selectionRate", Math.round(selectionRate * 10.0) / 10.0);

 
        String avgTimeQuery =
                "SELECT AVG(DATEDIFF(day, j.posted_date, ja.applied_date)) as avg_days " +
                "FROM Job_Application.M_S_Job_Application ja " +
                "JOIN Job_Application.M_D_Job j ON ja.job_id = j.job_id " +
                "WHERE j.recruiter_id = ? AND ja.status = 'Selected'";
        try (PreparedStatement ps = conn.prepareStatement(avgTimeQuery)) {
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            stats.put("avgTimeToHire", rs.next() ? rs.getInt("avg_days") : 0);
        }


        String topSkillQuery =
                "SELECT TOP 1 skills, COUNT(*) as skill_count " +
                "FROM Job_Application.M_D_Job " +
                "WHERE recruiter_id = ? AND skills IS NOT NULL AND skills != '' " +
                "GROUP BY skills ORDER BY skill_count DESC";
        try (PreparedStatement ps = conn.prepareStatement(topSkillQuery)) {
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getString("skills") != null) {
                String skills = rs.getString("skills");
                String topSkill = skills.contains(",") ? skills.split(",")[0].trim() : skills;
                stats.put("topSkill", topSkill);
            } else {
                stats.put("topSkill", "N/A");
            }
        }

      
        int[] monthlyApplications = new int[12];
        String monthlyQuery =
                "SELECT MONTH(ja.applied_date) as month, COUNT(*) as count " +
                "FROM Job_Application.M_S_Job_Application ja " +
                "JOIN Job_Application.M_D_Job j ON ja.job_id = j.job_id " +
                "WHERE j.recruiter_id = ? AND ja.applied_date >= DATEADD(month, -12, GETDATE()) " +
                "GROUP BY MONTH(ja.applied_date) ORDER BY MONTH(ja.applied_date)";
        try (PreparedStatement ps = conn.prepareStatement(monthlyQuery)) {
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int month = rs.getInt("month") - 1;
                if (month >= 0 && month < 12) {
                    monthlyApplications[month] = rs.getInt("count");
                }
            }
        }
        stats.put("monthlyApplications", monthlyApplications);

   //
     // ✅ NEW LOGIC — Calculate Active / Closed based on closing_date using WITH (NOLOCK)
        int active = 0;
        int closed = 0;

        String jobQuery = 
            "SELECT closing_date " +
            "FROM Job_Application.M_D_Job WITH (NOLOCK) " +
            "WHERE recruiter_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(jobQuery)) {
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();

            java.time.LocalDate today = java.time.LocalDate.now();

            while (rs.next()) {
                java.sql.Date closingDateSql = rs.getDate("closing_date");

                if (closingDateSql != null) {
                    java.time.LocalDate closingDate = closingDateSql.toLocalDate();

                    if (closingDate.isBefore(today)) {
                        closed++;
                    } else {
                        active++;
                    }
                }
            }
        }

        Map<String, Integer> statusDistribution = new HashMap<>();
        statusDistribution.put("active", active);
        statusDistribution.put("closed", closed);
        statusDistribution.put("draft", 0); 


        stats.put("jobStatusDistribution", statusDistribution);

        return stats;
    }

    private String escape(Object value) {
        if (value == null) return "";
        return value.toString()
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
