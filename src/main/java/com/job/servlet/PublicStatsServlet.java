package com.job.servlet;

import com.job.DAO.JobDAO;
import com.job.DAO.UserDao;
import com.job.DAO.RecruiterDao;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/PublicStatsServlet")
public class PublicStatsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PublicStatsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            JobDAO jobDao = new JobDAO();
            UserDao userDao = new UserDao();
            RecruiterDao recruiterDao = new RecruiterDao();

        
            int totalJobs = jobDao.countActiveJobs();                  
            int totalCandidates = userDao.getTotalUsersCount();            
            int totalCompanies = jobDao.getUniqueCompaniesCount();     
            int jobsFilled = jobDao.getFilledJobsCount();                

           
            LOGGER.info("Stats: Jobs=" + totalJobs + ", Candidates=" + totalCandidates + ", Companies=" + totalCompanies + ", Filled=" + jobsFilled);

            JsonObject stats = new JsonObject();
            stats.addProperty("totalJobs", totalJobs);
            stats.addProperty("totalCandidates", totalCandidates);
            stats.addProperty("totalCompanies", totalCompanies);
            stats.addProperty("jobsFilled", jobsFilled);

            out.print(stats.toString());
            LOGGER.info("PublicStatsServlet response sent successfully");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error in PublicStatsServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
            JsonObject errorResult = new JsonObject();
            errorResult.addProperty("totalJobs", 0);
            errorResult.addProperty("totalCandidates", 0);
            errorResult.addProperty("totalCompanies", 0);
            errorResult.addProperty("jobsFilled", 0);
            out.print(errorResult.toString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in PublicStatsServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
            JsonObject errorResult = new JsonObject();
            errorResult.addProperty("totalJobs", 0);
            errorResult.addProperty("totalCandidates", 0);
            errorResult.addProperty("totalCompanies", 0);
            errorResult.addProperty("jobsFilled", 0);
            out.print(errorResult.toString());
        }
    }
}