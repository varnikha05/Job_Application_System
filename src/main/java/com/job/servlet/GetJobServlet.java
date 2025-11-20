package com.job.servlet;

import com.google.gson.Gson;
import com.job.BO.Job;
import com.job.DAO.JobDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet("/GetJobServlet")
public class GetJobServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(GetJobServlet.class.getName());
    private final JobDAO jobDAO = new JobDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            int jobId = Integer.parseInt(request.getParameter("jobId"));
            Job job = jobDAO.getJobById(jobId);
            if (job != null) {
                String json = new Gson().toJson(job);
                out.print(json);
            } else {
                out.print("{\"error\":\"Job not found\"}");
            }
        } catch (Exception e) {
            logger.severe("Error fetching job details: " + e.getMessage());
            out.print("{\"error\":\"Unable to fetch job details.\"}");
        } finally {
            out.close();
        }
    }
}
