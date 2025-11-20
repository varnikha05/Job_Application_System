package com.job.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.job.BO.Recruiter;
import com.job.db.DBConnection;


import java.util.List;
import java.util.logging.Logger;

public class RecruiterDao {

	 private static final Logger log = Logger.getLogger(RecruiterDao.class.getName());
    public boolean addRecruiter(Recruiter recruiter) {
        String sql = "INSERT INTO Job_Application.M_D_Recruiter "
                   + "(recruiter_name, company_name,email, password, phone,designation, is_active) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 1)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, recruiter.getRecruiter_name());
            ps.setString(2, recruiter.getCompanyName());
            ps.setString(3, recruiter.getEmail());
            ps.setString(4, recruiter.getPassword());
            ps.setString(5, recruiter.getPhone());
            ps.setString(6, recruiter.getDesignation());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Recruiter> getAllRecruiters() {
        List<Recruiter> list = new ArrayList<>();
 
        String sql = "SELECT recruiter_id, recruiter_name, company_name, email, phone, designation, is_active "
                + "FROM Job_Application.M_D_Recruiter";
 
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
 
            while (rs.next()) {
                Recruiter r = new Recruiter();
                r.setRecruiterId(rs.getInt("recruiter_id"));
                r.setRecruiter_name(rs.getString("recruiter_name"));
                r.setCompanyName(rs.getString("company_name"));
                r.setEmail(rs.getString("email"));
                r.setPhone(rs.getString("phone"));
                r.setDesignation(rs.getString("designation"));
                r.setStatus(rs.getInt("is_active"));
                list.add(r);
            }
 
        } catch (Exception e) {
            log.severe("List recruiters failed: " + e.getMessage());
        }
 
        return list;
    }
    public int getTotalRecruitersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Job_Application.M_D_Recruiter WHERE is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    //  get recriter by id
    public Recruiter getRecruiterById(int id) {
        String sql = "SELECT recruiter_id, recruiter_name, company_name, email, phone, designation, is_active " +
                     "FROM Job_Application.M_D_Recruiter WHERE recruiter_id = ?";
 
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                Recruiter r = new Recruiter();
                r.setRecruiterId(rs.getInt("recruiter_id"));
                r.setRecruiter_name(rs.getString("recruiter_name"));
                r.setCompanyName(rs.getString("company_name"));
                r.setEmail(rs.getString("email"));
                r.setPhone(rs.getString("phone"));
                r.setDesignation(rs.getString("designation"));
                r.setStatus(rs.getInt("is_active"));
                return r;
            }
 
        } catch (Exception e) {
            log.severe("Get recruiter failed: " + e.getMessage());
        }
 
        return null;
    }
 
    //update recruiter
    public boolean updateRecruiter(Recruiter r) {
        String sql = "UPDATE Job_Application.M_D_Recruiter SET " +
                "recruiter_name=?, company_name=?, email=?, phone=?, designation=?, is_active=? " +
                "WHERE recruiter_id=?";
 
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
 
            ps.setString(1, r.getRecruiter_name());
            ps.setString(2, r.getCompanyName());
            ps.setString(3, r.getEmail());
            ps.setString(4, r.getPhone());
            ps.setString(5, r.getDesignation());
            ps.setInt(6, r.getStatus());
            ps.setInt(7, r.getRecruiterId());
 
            return ps.executeUpdate() > 0;
 
        } catch (Exception e) {
            log.severe("Update recruiter failed: " + e.getMessage());
            return false;
        }
    }
 
    public boolean deleteRecruiter(int id) {
        String sql = "UPDATE Job_Application.M_D_Recruiter SET is_active = 0 WHERE recruiter_id = ?"; 

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            log.severe("Delete recruiter failed: " + e.getMessage());
            return false;
        }
    }

}
    


