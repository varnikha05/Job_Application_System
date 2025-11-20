package com.job.servlet;

import com.google.gson.Gson;
import com.job.BO.Job;
import com.job.DAO.JobDAO;
 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.logging.Logger;
/**
 * Servlet implementation class EditJobServlet
 */
@WebServlet("/EditJobServlet")
public class EditJobServlet extends HttpServlet {
 
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(EditJobServlet.class.getName());
    private final JobDAO jobDAO = new JobDAO();
    
    public EditJobServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int jobId = Integer.parseInt(request.getParameter("jobId"));
            Job job = jobDAO.getJobById(jobId);

            if (job != null) {
                com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
                obj.addProperty("jobId", job.getJobId());
                obj.addProperty("title", job.getTitle());
                obj.addProperty("description", job.getDescription());
                obj.addProperty("location", job.getLocation());
                obj.addProperty("experience", job.getExperience());
                obj.addProperty("salary", job.getSalary());
                obj.addProperty("skills", job.getSkills());
                obj.addProperty("status", job.getStatus());
                obj.addProperty("jobType", job.getJobType());
                obj.addProperty("companyName", job.getCompanyName());
                obj.addProperty("companyWebsite", job.getCompanyWebsite());
                obj.addProperty("companyDescription", job.getCompanyDescription());
                obj.addProperty("postedDate",
                	    job.getPostedDate() == null ? "" : job.getPostedDate().toLocalDate().toString());

                obj.addProperty("closingDate", job.getClosingDate() == null ? "" : job.getClosingDate().toString());

                out.print(obj.toString());
            

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int jobId = Integer.parseInt(request.getParameter("jobId"));
            Job existingJob = jobDAO.getJobById(jobId);

            String title = request.getParameter("title");
            if (title == null || title.trim().isEmpty()) {
                title = existingJob.getTitle();
            }

            String description = request.getParameter("description");
            if (description == null || description.trim().isEmpty()) {
                description = existingJob.getDescription();
            }

            String location = request.getParameter("location");
            if (location == null || location.trim().isEmpty()) {
                location = existingJob.getLocation();
            }

            String experience = request.getParameter("experience");
            if (experience == null || experience.trim().isEmpty()) {
                experience = existingJob.getExperience();
            }

            String salary = request.getParameter("salary");
            if (salary == null || salary.trim().isEmpty()) {
                salary = existingJob.getSalary();
            }
            
   
            String[] skillArray = request.getParameterValues("skills");
            String skills;
            if (skillArray == null || skillArray.length == 0) {
                skills = existingJob.getSkills();
            } else {
                skills = String.join(",", skillArray);
            }
             
            

            String status = request.getParameter("status");
            if (status == null || status.trim().isEmpty()) {
                status = existingJob.getStatus();
            }
        
            String[] typeArray = request.getParameterValues("jobType");
            String jobType;
            if (typeArray == null || typeArray.length == 0) {
                jobType = existingJob.getJobType();
            } else {
                jobType = String.join(",", typeArray);
            }

            String companyName = request.getParameter("company_name");
            if (companyName == null || companyName.trim().isEmpty()) {
                companyName = existingJob.getCompanyName();
            }

            String companyWebsite = request.getParameter("company_website");
            if (companyWebsite == null || companyWebsite.trim().isEmpty()) {
                companyWebsite = existingJob.getCompanyWebsite();
            }

            String companyDescription = request.getParameter("company_description");
            if (companyDescription == null || companyDescription.trim().isEmpty()) {
                companyDescription = existingJob.getCompanyDescription();
            }

            String closingDate = request.getParameter("closing_date");
            LocalDate closingDateVal = null;
            if (closingDate == null || closingDate.trim().isEmpty()) {
                closingDateVal = existingJob.getClosingDate();
            } else {
                closingDateVal = LocalDate.parse(closingDate);
            }
            
            if (existingJob.getPostedDate() != null && closingDateVal != null) {
                LocalDate posted = existingJob.getPostedDate().toLocalDate();
                if (closingDateVal.isBefore(posted)) {
                	 response.setContentType("application/json");
                     response.getWriter().write("{\"error\":\"invalidClosingDate\"}");
                     return;
                }
            }
            Job job = new Job();
            job.setJobId(jobId);
            job.setTitle(title);
            job.setDescription(description);
            job.setLocation(location);
            job.setExperience(experience);
            job.setSalary(salary);
            job.setSkills(skills);
            job.setStatus(status);
            job.setJobType(jobType);
            job.setCompanyName(companyName);
            job.setCompanyWebsite(companyWebsite);
            job.setCompanyDescription(companyDescription);
            job.setClosingDate(closingDateVal);

            boolean updated = jobDAO.updateJob(job);

            if (updated) {
                response.sendRedirect("recruiter-view-jobs.html?msg=Job updated successfully");
            } else {
                response.getWriter().println("Error updating job. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Invalid data or server error.");
        }
    }}