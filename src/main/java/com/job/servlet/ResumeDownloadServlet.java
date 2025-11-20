package com.job.servlet;

import com.job.DAO.UserDao;
import com.job.db.DBConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ResumeDownloadServlet
 */
@WebServlet("/ResumeDownloadServlet")
public class ResumeDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResumeDownloadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    private static final Logger logger = Logger.getLogger(ResumeDownloadServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
 
        String appIdStr = request.getParameter("applicationId");
 
        if (appIdStr == null) {
            response.getWriter().print("Missing applicationId");
            return;
        }
 
        int applicationId = Integer.parseInt(appIdStr);

        String sql = "SELECT resume FROM Job_Application.M_S_Job_Application WHERE application_id = ?";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setInt(1, applicationId);
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                byte[] file = rs.getBytes("resume");
 
                if (file == null) {
                    response.getWriter().print("No resume uploaded");
                    return;
                }
 
                
                boolean preview = "true".equals(request.getParameter("preview"));
 
                if (preview) {
                    response.setHeader("Content-Disposition", "inline; filename=resume.pdf");
                } else {
                    response.setHeader("Content-Disposition", "attachment; filename=resume.pdf");
                }
 
                response.setContentType("application/pdf");
                response.getOutputStream().write(file);
            } else {
                response.getWriter().print("No record found");
            }
 
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("Server error");
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
