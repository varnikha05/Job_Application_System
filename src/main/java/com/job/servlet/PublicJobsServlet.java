package com.job.servlet;

import com.job.BO.Job;
import com.job.DAO.JobDAO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet to return list of public jobs for index.html
 */
@WebServlet("/PublicJobsServlet")
public class PublicJobsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PublicJobsServlet.class.getName());
    private final JobDAO jobDao = new JobDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        int limit = parseInt(request.getParameter("limit"), 10);
        int page = parseInt(request.getParameter("page"), 1);
        String title = request.getParameter("title");
        String location = request.getParameter("location");
        String jobType = request.getParameter("jobType"); // Added job type parameter
        int offset = (page - 1) * limit;

        try {
          
            int totalJobsInDB = jobDao.getTotalJobCount();
            LOGGER.info("Total jobs in database: " + totalJobsInDB);
         
            int activeJobsCount = jobDao.countActiveJobs();
            LOGGER.info("Active jobs count: " + activeJobsCount);
            
        
            List<Job> jobs = jobDao.getPublicJobs(limit, offset, title, location, jobType);
            int total = jobDao.countPublicJobs(title, location, jobType);

            JsonObject result = new JsonObject();
            result.addProperty("totalJobs", total);
            JsonArray arr = new JsonArray();

            for (Job j : jobs) {
                JsonObject obj = new JsonObject();
                obj.addProperty("jobId", j.getJobId());
                obj.addProperty("title", j.getTitle());
                obj.addProperty("companyName", j.getCompanyName());
                obj.addProperty("location", j.getLocation());
                obj.addProperty("salary", j.getSalary());
                obj.addProperty("status", j.getStatus());
                obj.addProperty("jobType", j.getJobType());
                obj.addProperty("summary", j.getDescription());
                arr.add(obj);
            }

            result.add("jobs", arr);
            out.print(result.toString());

            LOGGER.info("PublicJobsServlet returned " + jobs.size() + " jobs out of " + total + " total");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error in PublicJobsServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        
            JsonObject errorResult = new JsonObject();
            errorResult.addProperty("totalJobs", 0);
            errorResult.add("jobs", new JsonArray());
            errorResult.addProperty("error", "Database error: " + e.getMessage());
            out.print(errorResult.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in PublicJobsServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
           
            JsonObject errorResult = new JsonObject();
            errorResult.addProperty("totalJobs", 0);
            errorResult.add("jobs", new JsonArray());
            errorResult.addProperty("error", "Server error: " + e.getMessage());
            out.print(errorResult.toString());
        }
    }
    private int parseInt(String val, int def) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return def;
        }
    }
}
