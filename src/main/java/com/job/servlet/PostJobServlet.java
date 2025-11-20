package com.job.servlet;

import com.job.BO.Job;
import com.job.DAO.JobDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.logging.Logger;

@WebServlet("/PostJobServlet")
public class PostJobServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PostJobServlet.class.getName());
    private final JobDAO jobDao = new JobDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

       
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
           
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            if (description == null || description.trim().isEmpty()
            	    || "<p><br></p>".equals(description.trim())
            	    || "<p>Write Job Description!</p>".equals(description.trim())) {
            	    description = "<p>No description provided</p>";
            	}
            String location = request.getParameter("location");
            String experience = request.getParameter("experience");
            String salary = request.getParameter("salary");

          
            String[] skillArray = request.getParameterValues("skills");
            String skills = (skillArray != null) ? String.join(",", skillArray) : request.getParameter("skills");


            String[] jobTypeArray = request.getParameterValues("jobType");
            String jobType = (jobTypeArray != null) ? String.join(",", jobTypeArray) : null;

            String companyName = request.getParameter("company_name");
            String companyWebsite = request.getParameter("company_website");
            String companyDescription = request.getParameter("company_description");
            if (companyDescription == null || companyDescription.trim().isEmpty()) {
                companyDescription = "<p>No company description provided</p>";
            } else if (!companyDescription.contains("<")) {
             
                companyDescription = "<p>" + companyDescription.trim() + "</p>";
            }
            
            //String closingDate = request.getParameter("closing_date");

            String closingDateParam = request.getParameter("closing_date");
            LocalDate closingDate = null;
            if (closingDateParam != null && !closingDateParam.trim().isEmpty()) {
                try {
                    closingDate = LocalDate.parse(closingDateParam);
                    if (closingDate.isBefore(LocalDate.now())) {
                        response.sendRedirect("post-job.html?error=invalidClosingDate");
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Invalid date format: " + closingDateParam);
                }
            }
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("recruiterId") == null) {
                logger.warning("Session expired or recruiter not logged in.");
                response.sendRedirect("Employers.html?error=sessionExpired");
                return;
            }
            Integer recruiterId = (Integer) session.getAttribute("recruiterId");

          
            if (description == null || description.trim().isEmpty()) {
                description = "<p>Welcome All </p>";
            }

            Job job = new Job();
            job.setRecruiterId(recruiterId);
            job.setTitle(title);
            job.setDescription(description);
            job.setLocation(location);
            job.setExperience(experience);
            job.setSalary(salary);
            job.setSkills(skills);
            job.setJobType(jobType);
            job.setCompanyName(companyName);
            job.setCompanyWebsite(companyWebsite);
            job.setCompanyDescription(companyDescription);
            job.setClosingDate(closingDate);

            boolean posted = jobDao.postJob(job);

           
            if (posted) {
                logger.info("Job posted successfully by recruiter ID: " + recruiterId);
                response.sendRedirect("post-job.html?msg=success");
                return;
                
            } else {
                logger.warning("Job post failed for recruiter ID: " + recruiterId);
                response.sendRedirect("recruiter_dashboard.html?msg=failed");
            }

        } catch (Exception e) {
            logger.severe("Error posting job: " + e.getMessage());
            response.sendRedirect("recruiter_dashboard.html?msg=error");
        } 
    }
}
