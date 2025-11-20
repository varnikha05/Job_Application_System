package com.job.servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.job.BO.Employee;
import com.job.DAO.EmployeeDao;
import com.job.db.DBConnection;

@WebServlet("/EmployeeLoginServlet")
public class EmployeeLoginServlet extends HttpServlet {

    private EmployeeDao employeeDao = new EmployeeDao();
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


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = hashPassword(request.getParameter("password"));

        String role = request.getParameter("role");

        Employee emp = employeeDao.loginEmployee(email, password, role);

        response.setContentType("text/plain; charset=UTF-8");

        if (emp != null) {

            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(1800);

           
            session.setAttribute("role", role);
            if ("recruiter".equals(role)) {
            	                session.setAttribute("recruiterSession", emp);
            	               session.setAttribute("recruiterId", emp.getId());
            	           } else if ("admin".equals(role)) {
            	               session.setAttribute("adminSession", emp);
            	               session.setAttribute("adminId", emp.getId());
                        }

//            
//            if (role.equals("recruiter")) {
//                session.setAttribute("recruiterId", emp.getId());
//                session.removeAttribute("adminId");
//            }
//
//            if (role.equals("admin")) {
//                session.setAttribute("adminId", emp.getId());
//                session.removeAttribute("recruiterId");
//                
//            }
            
        

            
            if ("admin".equals(role)) {
                response.getWriter().print("success-admin");
            } 
            else if ("recruiter".equals(role)) {
            	response.getWriter().print("success-recruiter");
            } 
            else {
                response.getWriter().print("fail");
            }
        
        }
        else {
        	       
        	          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        	            response.getWriter().print("Invalid credentials or role");
        }
        
    }
}
