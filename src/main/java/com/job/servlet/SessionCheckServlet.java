package com.job.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/SessionCheckServlet")
public class SessionCheckServlet extends HttpServlet {

	

	    @Override
	    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {
	    	 response.setContentType("application/json");

	    	 HttpSession session = request.getSession(false);
	    	 
	         boolean user = false, admin = false, recruiter = false, loggedIn = false;
	  
	         if (session != null) {
	             user = session.getAttribute("userSession") != null;
	             admin = session.getAttribute("adminSession") != null;
	             recruiter = session.getAttribute("recruiterSession") != null;
	  
	             loggedIn = user || admin || recruiter;
	             String role=null;
	             if(user) role ="user";
	             if(admin) role ="admin";
	             if(recruiter) role ="recruiter";
	             
	         
	  
	         String json = "{"
	                 + "\"loggedIn\": " + loggedIn + ","
	                 + "\"user\": " + user + ","
	                 + "\"admin\": " + admin + ","
	                 + "\"recruiter\": " + recruiter  + ","
	                  + "\"role\": \"" + role +"\""
	                 + "}";
	  
	         response.getWriter().print(json);
	     }
	           }
}