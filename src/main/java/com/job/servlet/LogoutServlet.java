package com.job.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * LogoutServlet - Handles user logout
 * Invalidates session and redirects to landing page
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	HttpSession session = request.getSession(false);
    	       String type = request.getParameter("type");
    	       if (session != null) {
    	    	              if ("user".equalsIgnoreCase(type)) {
    	    	                   session.removeAttribute("userSession");
    	    	                   session.removeAttribute("userId");
    	    	                   session.removeAttribute("email");
    	    	                   response.sendRedirect("login.html");
    	    	                   return;
    	    	               }
    	    	               if ("admin".equalsIgnoreCase(type)) {
    	    	                   session.removeAttribute("adminSession");
    	    	                   session.removeAttribute("adminId");
    	    	                   response.sendRedirect("Employers.html");
    	    	                   return;
    	    	               }
    	    	               if ("recruiter".equalsIgnoreCase(type)) {
    	    	                  session.removeAttribute("recruiterSession");
    	    	                   session.removeAttribute("recruiterId");
    	    	                  response.sendRedirect("Employers.html");
    	    	                   return;
    	    	               }
    	    	               if ("all".equalsIgnoreCase(type)) {
    	    	           
    	    	                   session.invalidate();
    	    	                   response.sendRedirect("index.html");
    	    	                   return;
    	    	               }
    	    	           }
    	    	         
    	    	           response.sendRedirect("index.html");
    	    	        }
    	    	    }
