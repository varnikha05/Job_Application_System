package com.job.servlet;

 
import com.job.DAO.JobDAO;

import com.job.BO.Job;
import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class JobDetailsServlet
 */
@WebServlet("/JobDetailsServlet")
public class JobDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final JobDAO jobDao = new JobDAO();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public JobDetailsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        response.setContentType("application/json; charset=UTF-8");
        int jobid = Integer.parseInt(request.getParameter("job_id"));
        Gson gson = new Gson();
 
        try {
            Job job = jobDao.getJobById(jobid);
            String json = gson.toJson(job);
            response.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"error\":\"Unable to fetch job details.\"}");
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
