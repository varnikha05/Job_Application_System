package com.job.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.job.BO.User;
import com.job.DAO.UserDao;
import com.job.db.DBConnection;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpSession;


@WebServlet("/User_LoginServlet")
public class User_LoginServlet extends HttpServlet {
	
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


        UserDao userDao = new UserDao();
        User user = userDao.LoginUser(email, password);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (user != null) {

        	 HttpSession session = request.getSession(true);
        	  session.setAttribute("loggedIn", true);
        	  session.setAttribute("userSession", user);
        	  session.setAttribute("role", "user");
        	  
        
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("email", user.getEmail());
          //  session.setMaxInactiveInterval(1800);
            
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Job_Application.M_D_User SET last_updated = GETDATE() WHERE user_id = ?");
                ps.setInt(1, user.getUserId());
                ps.executeUpdate();
            }catch (Exception e) {
            	e.printStackTrace();
            }

            out.print("{\"status\":\"success\",\"redirect\":\"UserDashboard.html\"}");
        } else {
            out.print("{\"status\":\"fail\",\"message\":\"Invalid email or password.\"}");
        }
    }
}
