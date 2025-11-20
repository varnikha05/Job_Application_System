package com.job.servlet;

import com.job.DAO.UserDao;
import com.job.BO.User;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@WebServlet("/ManageUsersServlet")
@MultipartConfig
public class ManageUsersServlet extends HttpServlet {
    private UserDao userDao = new UserDao();
    Gson gson = new Gson();
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null ||session.getAttribute("adminSession") == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
            return;
        }

        List<User> users = userDao.getAllUsers();
        
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(users));
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminSession") == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
            return;
        }

        String method = request.getParameter("_method");

        try {

            if ("PUT".equalsIgnoreCase(method)) {

                int userId = Integer.parseInt(request.getParameter("userId"));

                User user = new User();
                user.setUserId(userId);
                user.setFull_name(request.getParameter("full_name"));
                user.setEmail(request.getParameter("email"));
                user.setPhone(request.getParameter("phone"));
               // user.setGender(request.getParameter("gender"));
               // user.setQualification(request.getParameter("qualification"));
                user.setSkills(request.getParameter("skills"));
                user.setStatus(request.getParameter("status")); // "1" or "0"

                userDao.updateUser(user);
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            else if ("DELETE".equalsIgnoreCase(method)) {

                int userId = Integer.parseInt(request.getParameter("userId"));
                userDao.deleteUser(userId);
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            else {
                // ADD USER
                User user = new User();
                user.setFull_name(request.getParameter("full_name"));
                user.setEmail(request.getParameter("email"));
                user.setPassword(hashPassword(request.getParameter("password")));
                user.setPhone(request.getParameter("phone"));
               user.setGender(request.getParameter("gender"));
               user.setQualification(request.getParameter("qualification"));
                user.setSkills(request.getParameter("skills"));

                userDao.addUser(user);
                response.setStatus(HttpServletResponse.SC_OK);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}