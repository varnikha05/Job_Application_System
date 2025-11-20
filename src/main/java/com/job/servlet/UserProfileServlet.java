package com.job.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;
import com.job.BO.User;
import com.job.db.DBConnection;

/**
 * Servlet implementation class UserProfileServlet
 */
@WebServlet("/UserProfileServlet")
public class UserProfileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserProfileServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        HttpSession session = request.getSession(false);
 
      
        User loggedUser = (session != null) ? (User) 
        		session.getAttribute("userSession") : null;
 
        if (loggedUser == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"loggedIn\":false}");
            return;
        }
 
        int userId = loggedUser.getUserId();  
 
        try (Connection conn = DBConnection.getConnection()) {
        	String sql = "SELECT full_name, email, phone, skills, qualification FROM Job_Application.M_D_User WHERE user_id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
 
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                response.setContentType("application/json");
                response.getWriter().write(
                        "{"
                                + "\"loggedIn\":true,"
                                + "\"fullName\":\"" + rs.getString("full_name") + "\","
                                + "\"email\":\"" + rs.getString("email") + "\","
                                + "\"phone\":\"" + rs.getString("phone") + "\","
                                + "\"qualification\":\"" + rs.getString("qualification") + "\","
                                + "\"skills\":\"" + rs.getString("skills") + "\""
                                + "}"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"error\":\"server-error\"}");
        }
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
