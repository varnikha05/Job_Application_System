package com.job.servlet;

import com.google.gson.Gson;
import com.job.DAO.JobDAO;
import com.job.BO.Job;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/RecruiterJobsServlet")
public class RecruiterJobsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(RecruiterJobsServlet.class.getName());
    private final JobDAO jobDao = new JobDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
    	
    	
    	



        try {
            HttpSession session = request.getSession(false);

            if (session == null || !"recruiter".equals(session.getAttribute("role"))) {
                response.sendError(403, "Unauthorized");
                return;
            }

            Integer recruiterId = (Integer) session.getAttribute("recruiterId");

        	if (recruiterId == null || recruiterId <= 0) {
        	    System.out.println("Recruiter ID missing in session");
        	    return;
        	}

            // ✅ Fetch jobs normally
            List<Job> jobs = jobDao.getJobsByRecruiter(recruiterId);

            String json = new Gson().toJson(jobs);
            out.print(json);

        } catch (Exception e) {
            logger.severe("Error fetching recruiter jobs: " + e.getMessage());
            out.print("{\"error\":\"Unable to load jobs. Try again later.\"}");
        } finally {
            out.close();
        }
    }
}
