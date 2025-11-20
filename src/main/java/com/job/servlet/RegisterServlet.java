package com.job.servlet;
 
import com.job.BO.User;
import com.job.DAO.UserDao;
 
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.logging.Logger;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

 
@WebServlet("/register")
@MultipartConfig
public class RegisterServlet extends HttpServlet {
 
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(RegisterServlet.class.getName());
    private final UserDao userDao = new UserDao();
    
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
 
        response.setContentType("text/plain; charset=UTF-8");
        PrintWriter out = response.getWriter();
 
        try {
          
            String name = request.getParameter("full_name");
            String email = request.getParameter("email");
            String password = hashPassword(request.getParameter("password"));

            String phone = request.getParameter("phone");
            String gender = request.getParameter("gender");
            String qualification = request.getParameter("qualification");
            String[] skillsArr = request.getParameterValues("skills[]");
            String skills = (skillsArr != null) ? String.join(",", skillsArr) : "";
 
      
            if (name == null || name.trim().isEmpty()) {
                out.println("Full name is required."); return;
            }
            if (email == null || email.trim().isEmpty()) {
                out.println("Email is required."); return;
            }
            if (password == null || password.trim().isEmpty()) {
                out.println("Password is required."); return;
            }
 
      
          
            
            Part resumePart = request.getPart("resume");
            byte[] resumeBytes = null;
             
            if (resumePart != null && resumePart.getSize() > 0) {
                logger.info("Resume uploaded: " + resumePart.getSubmittedFileName() + " | Size: " + resumePart.getSize());
                try (InputStream input = resumePart.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
             
                    byte[] chunk = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = input.read(chunk)) != -1) {
                        buffer.write(chunk, 0, bytesRead);
                    }
                    resumeBytes = buffer.toByteArray();
                    logger.info("Resume bytes captured: " + resumeBytes.length);
                }
            } else {
                logger.warning("No resume file uploaded!");
                out.println("Please upload your resume.");
                return;
            }
        
            if (userDao.emailExists(email)) {
                out.println("Email already exists! Please use a different email.");
                return;
            }

            User user = new User();
            user.setFull_name(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setPhone(phone);
            user.setGender(gender);
            user.setQualification(qualification);
            user.setSkills(skills);
            user.setResumeBytes(resumeBytes); // byte array field
 
       
            boolean saved = userDao.registerUser(user);
 
         
            if (saved) {
                out.println("success");
                logger.info("User registered: " + email);
            } else {
                out.println("Registration failed! Email may already exist.");
                logger.warning("Duplicate email: " + email);
            }
 
        } catch (Exception e) {
            logger.severe("Registration error: " + e.getMessage());
            out.println("An unexpected error occurred.");
        } finally {
            out.close();
        }
    }
}

 