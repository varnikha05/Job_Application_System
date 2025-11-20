package com.job.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.job.BO.Admin;
import com.job.db.DBConnection;

public class AdminDao {
	 private static final Logger log = Logger.getLogger(RecruiterDao.class.getName());

    public boolean addAdmin(Admin admin) {
        String sql = "INSERT INTO Job_Application.M_D_Admin "
                   + "(admin_name, email, password, phone, is_active) "
                   + "VALUES (?, ?, ?, ?, 1)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, admin.getAdmin_name());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getPassword());
            ps.setString(4, admin.getPhone());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Admin> getAllAdmins() {
        List<Admin> list = new ArrayList<>();

        String sql = "SELECT admin_id, admin_name, email, phone, role, created_date, is_active "
                   + "FROM Job_Application.M_D_Admin ";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Admin a = new Admin();

                a.setAdminId(rs.getInt("admin_id"));
                a.setAdmin_name(rs.getString("admin_name"));
                a.setEmail(rs.getString("email"));
                a.setPhone(rs.getString("phone"));
                a.setRole(rs.getString("role"));
                a.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());

                a.setActive(rs.getInt("is_active") == 1);

                list.add(a);
            }

        } catch (Exception e) {
            log.severe("getAllAdmins() failed: " + e.getMessage());
        }

        return list;
    }
    public boolean updateAdmin(Admin a) {
        String sql = "UPDATE Job_Application.M_D_Admin SET "
                   + "admin_name=?, email=?, phone=?, is_active=?, "
                   + "last_updated = GETDATE() "
                   + "WHERE admin_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, a.getAdmin_name());
            ps.setString(2, a.getEmail());
            ps.setString(3, a.getPhone());
            ps.setInt(4, a.isActive() ? 1 : 0);

            ps.setInt(5, a.getAdminId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            log.severe("updateAdmin failed: " + e.getMessage());
            return false;
        }
    }
    public boolean deleteAdmin(int adminId) {
        String sql = "UPDATE Job_Application.M_D_Admin "
                   + "SET is_active = 0, last_updated = GETDATE() "
                   + "WHERE admin_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            log.severe("deleteAdmin failed: " + e.getMessage());
            return false;
        }
    }


}
