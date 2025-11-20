package com.job.DAO;

import com.job.db.DBConnection;
import java.sql.*;
 
public class AdminStatsDao {
 
    public int countUsers() throws Exception {
        String sql = "SELECT COUNT(*) FROM Job_Application.M_D_User WHERE is_active = 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
 
    public int countRecruiters() throws Exception {
       
        String sql = "SELECT COUNT(*) FROM Job_Application.M_D_Recruiter WHERE is_active = 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
 
    public int countJobs() throws Exception {
        String sql = "SELECT COUNT(*) FROM Job_Application.M_D_Job WHERE is_active = 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
 
    public int countApplications() throws Exception {
        String sql = "SELECT COUNT(*) FROM Job_Application.M_S_Job_Application WHERE is_active = 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
