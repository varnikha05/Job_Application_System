package com.job.servlet;

 
import com.job.db.DBConnection;
 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
/**
 * Servlet implementation class ApplicationResueDownloadServlet
 */

@WebServlet("/ApplicationResumeDownloadServlet")
public class ApplicationResumeDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
       
  

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String appIdStr = request.getParameter("applicationId");
        if (appIdStr == null || appIdStr.isEmpty()) {
            response.getWriter().write("Invalid Application ID");
            return;
        }

        int applicationId = Integer.parseInt(appIdStr);

        
        String sql =
            "SELECT ja.resume, u.full_name " +
            "FROM Job_Application.M_S_Job_Application ja " +
            "JOIN Job_Application.M_D_User u ON ja.user_id = u.user_id " +
            "WHERE ja.application_id = ? AND ja.is_active = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    byte[] resumeBytes = rs.getBytes("resume");
                    String fullName = rs.getString("full_name");

                    if (resumeBytes == null || resumeBytes.length == 0) {
                        response.getWriter().write("No resume uploaded.");
                        return;
                    }

                    if (fullName == null || fullName.trim().isEmpty()) {
                        fullName = "Applicant";
                    }

                    String fileName = fullName.replaceAll("\\s+", "_") + "_Resume.pdf";

                    response.setContentType("application/pdf");
                    response.setHeader("Content-Disposition",
                            "attachment; filename=\"" + fileName + "\"");
                    response.setContentLength(resumeBytes.length);

                    OutputStream os = response.getOutputStream();
                    os.write(resumeBytes);
                    os.flush();
                    os.close();

                } else {
                    response.getWriter().write("Application not found.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("Error retrieving resume.");
        }
    }



	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
