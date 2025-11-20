package com.job.servlet;

import com.job.db.DBConnection;
 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ApplicationResumePreviewServlet
 */
@WebServlet("/ApplicationResumePreviewServlet")
public class ApplicationResumePreviewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ApplicationResumePreviewServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        String appIdStr = request.getParameter("applicationId");
        if (appIdStr == null) {
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().print("Missing applicationId");
            return;
        }
 
        int applicationId = Integer.parseInt(appIdStr);
 
        String sql = "SELECT resume FROM Job_Application.M_S_Job_Application WHERE application_id = ? AND is_active = 1";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] bytes = rs.getBytes("resume");
                    if (bytes == null || bytes.length == 0) {
                        response.setContentType("text/plain; charset=UTF-8");
                        response.getWriter().print("No resume uploaded.");
                        return;
                    }
 
                 
                    boolean isPdf = bytes.length > 4 &&
                            bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
 
                    if (isPdf) {
                        response.setContentType("application/pdf");
                        response.setHeader("Content-Disposition", "inline; filename=resume.pdf");
                    } else {
                    
                        response.setContentType("application/octet-stream");
                        response.setHeader("Content-Disposition", "inline; filename=resume");
                    }
 
                    response.setContentLength(bytes.length);
                    try (OutputStream os = response.getOutputStream()) {
                        os.write(bytes);
                        os.flush();
                    }
                } else {
                    response.setContentType("text/plain; charset=UTF-8");
                    response.getWriter().print("Application not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().print("Server error.");
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
