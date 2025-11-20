package com.job.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.job.BO.Admin;
import com.job.BO.Recruiter;
import com.job.DAO.AdminDao;
import com.job.DAO.RecruiterDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;



@WebServlet("/ManageAdminsServlet")
public class ManageAdminsServlet extends HttpServlet {

    private AdminDao adminDao = new AdminDao();
    private RecruiterDao recruiterDao;
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	//        List<Admin> admins = adminDao.getAllAdmins();
//        resp.setContentType("application/json");
    	Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                	@Override
                    public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {    
                		return new JsonPrimitive(src.toString());  
                   }
              })
                .create();
//        String json = gson.toJson(admins);
//        resp.getWriter().write(json);

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminSession") == null) {
            resp.sendRedirect("Employers.html");
            return;
        }
 
        String ajax = req.getHeader("X-Requested-With");
 
        if ("XMLHttpRequest".equals(ajax)) {
         
            List<Admin> admins = adminDao.getAllAdmins();
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(admins));
            return;
        }
    }
        
       

       


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String method = req.getParameter("_method");

        if (method == null) {
            resp.setStatus(400);
            resp.getWriter().write("Missing _method");
            return;
        }

        switch (method) {

            case "PUT":
                update(req, resp);
                break;

            case "DELETE":
                delete(req, resp);
                break;

            default:
                resp.setStatus(405);
                resp.getWriter().write("Unsupported");
        }
    }

    private void update(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            Admin a = new Admin();

            a.setAdminId(Integer.parseInt(req.getParameter("adminId")));
            a.setAdmin_name(req.getParameter("admin_name"));
            a.setEmail(req.getParameter("email"));
            a.setPhone(req.getParameter("phone"));

            String st = req.getParameter("active");
            if (st == null) st = req.getParameter("status");

            boolean isActive =
                    "1".equals(st) ||
                    "true".equalsIgnoreCase(st) ||
                    "active".equalsIgnoreCase(st);

            a.setActive(isActive);

            boolean ok = adminDao.updateAdmin(a);
            resp.getWriter().write(ok ? "success" : "fail");

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("error");
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(req.getParameter("adminId"));
            boolean ok = adminDao.deleteAdmin(id);
            resp.getWriter().write(ok ? "success" : "fail");

        } catch (Exception e) {
            resp.setStatus(500);
            resp.getWriter().write("error");
        }
    }
}
