package com.job.servlet;

import com.google.gson.Gson;
import com.job.BO.Recruiter;
import com.job.DAO.RecruiterDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/ManageRecruitersServlet")
public class ManageRecruitersServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ManageRecruitersServlet.class.getName());
    private RecruiterDao recruiterDao;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        recruiterDao = new RecruiterDao();
        gson = new Gson();
        log.info("ManageRecruitersServlet initialized");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Recruiter> recruiters = recruiterDao.getAllRecruiters();
            String json = gson.toJson(recruiters);

            resp.setContentType("application/json");
            resp.getWriter().write(json);

        } catch (Exception e) {
            log.severe("GET failed: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String method = req.getParameter("_method");
        if (method == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid request");
            return;
        }

        switch (method) {
            case "PUT":
                updateRecruiter(req, resp);
                break;

            case "DELETE":
                deleteRecruiter(req, resp);
                break;

            default:
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Unsupported method");
        }
    }

 
    private void updateRecruiter(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(req.getParameter("recruiterId"));
           // String name = req.getParameter("recruiter_name");
            String company = req.getParameter("companyName");
            String email = req.getParameter("email");
         //   String phone = req.getParameter("phone");
            String designation = req.getParameter("designation");
            int status = Integer.parseInt(req.getParameter("status"));  // Active = 1

            Recruiter old = recruiterDao.getRecruiterById(id);
            
            Recruiter r = new Recruiter();
            r.setRecruiterId(id);
          //  r.setRecruiter_name(name);
            r.setRecruiter_name(old.getRecruiter_name());
            r.setCompanyName(company);
            r.setEmail(email);
           // r.setPhone(phone);
            r.setDesignation(designation);
            r.setStatus(status);

            boolean updated = recruiterDao.updateRecruiter(r);

            resp.setContentType("text/plain");
            resp.getWriter().write(updated ? "success" : "fail");

        } catch (Exception e) {
            log.severe("Update failed: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("error");
        }
    }

    private void deleteRecruiter(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(req.getParameter("recruiterId"));

            boolean deleted = recruiterDao.deleteRecruiter(id);

            resp.setContentType("text/plain");
            resp.getWriter().write(deleted ? "success" : "fail");

        } catch (Exception e) {
            log.severe("Delete failed: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("error");
        }
    }
}
