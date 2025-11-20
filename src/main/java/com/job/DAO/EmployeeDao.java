package com.job.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.job.BO.Employee;
import com.job.db.DBConnection;

public class EmployeeDao {
	 
	 public Employee loginEmployee(String email, String password, String role) {
	        String sql = "";
	 
			if ("admin".equalsIgnoreCase(role)) {
				sql = "SELECT admin_id AS id, admin_name AS name, email, password, is_active FROM Job_Application.M_D_Admin WHERE email=? AND password=? AND is_active=1";
			} else if ("recruiter".equalsIgnoreCase(role)) {
				sql = "SELECT recruiter_id AS id, recruiter_name AS name, email, password, is_active FROM Job_Application.M_D_Recruiter WHERE email=? AND password=? AND is_active=1";
	        } else {
	            return null; 
	        }
	 
	        try (Connection con = DBConnection.getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	 
	            ps.setString(1, email);
	            ps.setString(2, password);
	            ResultSet rs = ps.executeQuery();
	 
	            if (rs.next()) {
					Employee emp = new Employee();
					emp.setId(rs.getInt("id"));
					emp.setName(rs.getString("name"));
					emp.setEmail(rs.getString("email"));
					emp.setPassword(rs.getString("password"));
					emp.setRole(role);
	                return emp;
	            }
	 
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	 
	        return null; 
	    }


}
