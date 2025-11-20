package com.job.servlet;

import com.job.BO.Recruiter;
import com.job.DAO.RecruiterDao;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;



@MultipartConfig
@WebServlet("/AddRecruiterServlet")
public class AddRecruiterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private RecruiterDao recruiterDao = new RecruiterDao();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println("Invalid access.");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain; charset=UTF-8");

       
        String name = request.getParameter("recruiter_name");
        String company = request.getParameter("company_name");
        String email = request.getParameter("email");
        String password = hashPassword(request.getParameter("password"));

        String phone = request.getParameter("phone");
        String designation = request.getParameter("designation");

//        // 
//        if (name == null || name.isEmpty() || company == null || company.isEmpty() ||
//            email == null || email.isEmpty() || password == null || password.isEmpty() ||
//            phone == null || phone.isEmpty()) {
//            response.getWriter().println("Please fill all required fields.");
//            return;
//        }

        // Create Recruiter object
        Recruiter recruiter = new Recruiter();
        recruiter.setRecruiter_name(name);
        recruiter.setCompanyName(company);
        recruiter.setEmail(email);
        recruiter.setPassword(password);
        recruiter.setPhone(phone);
        recruiter.setDesignation(designation);


        boolean success = recruiterDao.addRecruiter(recruiter);

        if (success) {
            response.getWriter().println("success"); 
        } else {
            response.getWriter().println("Registration failed! Email may already exist.");
        }
    }
}
