package com.job.servlet;

import com.job.BO.Admin;
import com.job.DAO.AdminDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@WebServlet("/AddAdminServlet")
public class AddAdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AdminDao adminDao = new AdminDao();
    
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


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
        request.setCharacterEncoding("UTF-8");
        
       


        String name = request.getParameter("user_name");
        String email = request.getParameter("email");
        String password = hashPassword(request.getParameter("password"));

        String phone = request.getParameter("phone");

        Admin admin = new Admin();
        admin.setAdmin_name(name);
        admin.setEmail(email);
        admin.setPassword(password);
        admin.setPhone(phone);

        boolean success = adminDao.addAdmin(admin);

        if (success) {
            response.getWriter().write("success");
        } else {
            response.getWriter().write("Failed to add admin. Check console for errors.");
        }
    }
}
