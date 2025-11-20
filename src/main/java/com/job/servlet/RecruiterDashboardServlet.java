package com.job.servlet;


import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.google.gson.Gson;
import com.job.BO.Job;
import com.job.DAO.JobDAO;
/**
 * Servlet implementation class RecruiterDashboardServlet
 */
@WebServlet("/RecruiterDashboardServlet")
public class RecruiterDashboardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	 
	private JobDAO jobDao = new JobDAO();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecruiterDashboardServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    
    	  HttpSession session = request.getSession(false);
          if (session == null || session.getAttribute("recruiterSession") == null) {
              response.sendRedirect("Employers.html");
              return;
          }
   
          String ajax = request.getHeader("X-Requested-With");
        
          if ("XMLHttpRequest".equals(ajax)) {
   
              List<Job> jobs = jobDao.getJobsByRecruiter((int) session.getAttribute("recruiterId"));
   
              response.setContentType("application/json");
              response.getWriter().write(new Gson().toJson(jobs));
              return;
          }
   
   
    
        request.getRequestDispatcher("recruiter_dashboard.html").forward(request, response);
    }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
