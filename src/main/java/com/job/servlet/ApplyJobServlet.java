package com.job.servlet;

import com.job.BO.Employee;
import com.job.DAO.JobApplicationDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import javax.servlet.http.Part;
import java.io.*;

@WebServlet("/ApplyJobServlet")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024) 
public class ApplyJobServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final JobApplicationDao dao = new JobApplicationDao();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userSession") == null) {
            response.getWriter().write("{\"applied\":false}");
            return;
        }

        Object loggedUser = session.getAttribute("userSession");
        int userId;

        if (loggedUser instanceof com.job.BO.User) {
            userId = ((com.job.BO.User) loggedUser).getUserId();
        } else {
            response.getWriter().write("{\"applied\":false}");
            return;
        }

        String jobIdStr = request.getParameter("jobId");
        if (jobIdStr == null || jobIdStr.isEmpty()) {
            response.getWriter().write("{\"applied\":false}");
            return;
        }

        int jobId = Integer.parseInt(jobIdStr);

        try {
            boolean alreadyApplied = dao.userHasActiveApplication(jobId, userId);
            response.getWriter().write("{\"applied\":" + alreadyApplied + "}");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"applied\":false}");
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("text/plain; charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userSession")== null) {
            response.getWriter().write("Please login as user before applying.");
            return;
        }

        Object loggedUser = session.getAttribute("userSession");
        PrintWriter out = response.getWriter();
        int userId;

        if (loggedUser instanceof com.job.BO.Employee) {
            userId = ((com.job.BO.Employee) loggedUser).getId();
        } else if (loggedUser instanceof com.job.BO.User) {
            userId = ((com.job.BO.User) loggedUser).getUserId(); // or getId() if your BO class uses getId()
        } else if (loggedUser instanceof Integer) {
            userId = (Integer) loggedUser;
        } else {
            System.out.println("Unknown session type: " + loggedUser.getClass().getName());
            out.print("Invalid user session. Please log in again.");
            return;
        }


        String jobIdStr = request.getParameter("jobId");
        if (jobIdStr == null || jobIdStr.isEmpty()) {
            out.print("Invalid job ID.");
            return;
        }

        int jobId = Integer.parseInt(jobIdStr);

        String name = request.getParameter("applicantName");
        String email = request.getParameter("applicantEmail");
        String phone = request.getParameter("applicantPhone");
        String qualification = request.getParameter("qualification");
        String location = request.getParameter("location");
        String coverLetter = request.getParameter("coverLetter");


        String[] skillArray = request.getParameterValues("skills");
        String skills = "";
        if (skillArray != null && skillArray.length > 0) {
            skills = String.join(",", skillArray);
        }

  
        Part resumePart = request.getPart("resume"); 
        byte[] resumeBytes = null;
        if (resumePart != null && resumePart.getSize() > 0) {
            try (InputStream is = resumePart.getInputStream()) {
                resumeBytes = toByteArray(is);
            }
        }

        try {
            boolean applied = dao.applyJob(
                userId, jobId, name, email, phone, qualification,
                skills, location, coverLetter, resumeBytes
            );

            if (applied) {
                String json = "{"
                        + "\"status\":\"success\","
                        + "\"redirect\":\"user-job-details.html?jobId=" + jobId + "\""
                        + "}";
                out.print(json);
             
            } else {
                out.print("{\"status\":\"fail\",\"message\":\"You have already applied for this job.\"}");
            }
             
            System.out.println("Application saved for USER=" + userId + " JOB=" + jobId);

        } catch (Exception e) {
            e.printStackTrace();
            out.print("ERROR: Unable to apply for job. Please try again.");
        }
    }

    private static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
