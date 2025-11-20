package com.job.servlet;

import com.job.BO.Job;
import com.job.DAO.JobDAO;
import com.job.service.JobService;
import com.google.gson.Gson;  
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * Servlet implementation class UserDashboardServlet
 */
@WebServlet("/UserDashboardServlet")
public class UserDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final JobService jobService = new JobService();
    private final JobDAO jobDao = new JobDAO();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserDashboardServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
      
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userSession") == null) {
            response.sendRedirect("login.html");
            return;
        }
        String ajax = request.getHeader("X-Requested-With");
        
        if ("XMLHttpRequest".equals(ajax)) {
     
            String title = request.getParameter("title");
            String location = request.getParameter("location");
     
            List<Job> jobs = jobDao.getActiveJobs(title, location);
     
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(jobs));
            return;
        }
     
        request.getRequestDispatcher("UserDashboard.html").forward(request, response);
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
