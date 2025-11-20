package com.job.DAO;
 
import com.job.BO.User;
import com.job.db.DBConnection;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
 
public class UserDao {
 
    public boolean registerUser(User user) {
        String sql = "INSERT INTO Job_Application.M_D_User (full_name, email, password, phone, gender, qualification, skills, resume, created_date, last_updated, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE(), 1)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getFull_name());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getGender());
            ps.setString(6, user.getQualification());
            ps.setString(7, user.getSkills());
            ps.setBytes(8, user.getResumeBytes());
            int inserted = ps.executeUpdate();
            return inserted > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
 
    // Login user
    public User LoginUser(String email, String password) {
    	System.out.println("login attempt " +email+ "/"+password);
        String sql = "SELECT * FROM Job_Application.M_D_User WITH (NOLOCK) WHERE email = ? AND password = ? AND is_active = 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
            	System.out.println("user found db " +rs.getString("email"));

                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFull_name(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setGender(rs.getString("gender"));
                user.setQualification(rs.getString("qualification"));
                user.setSkills(rs.getString("skills"));
                byte[] resumeBytes = rs.getBytes("resume");
                user.setResumeBytes(resumeBytes);
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getResumePath(int userId) throws SQLException {
        String sql = "SELECT resume FROM Job_Application.M_D_User WITH (NOLOCK) WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("resume");
                }
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, phone, skills, is_active, created_date FROM Job_Application.M_D_User WITH (NOLOCK) where is_active=1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFull_name(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setSkills(rs.getString("skills"));

                // Status ACTIVE / INACTIVE
                user.setStatus(rs.getInt("is_active") == 1 ? "Active" : "Inactive");
               // user.setStatus(rs.getInt("is_active") );
                // Registration Date
                user.setRegistered(
                    rs.getTimestamp("created_date") != null ?
                    rs.getTimestamp("created_date").toString() : ""
                );

                users.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }



    public boolean addUser(User user) {
        String sql = "INSERT INTO Job_Application.M_D_User " +
                "(full_name, email, password, phone, gender, qualification, skills, created_date, last_updated, is_active) " +
                "VALUES (?, ?, ?, ?, ?,?, ?, GETDATE(), GETDATE(), ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getFull_name());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());   
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getGender());
            ps.setString(6, user.getQualification());
            ps.setString(7, user.getSkills());
            ps.setInt(8, user.getStatus() != null && user.getStatus().equalsIgnoreCase("Inactive") ? 0 : 1);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE Job_Application.M_D_User SET full_name=?, email=?, phone=?, skills=?, last_updated=GETDATE() WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getFull_name());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getSkills());
            ps.setInt(5, user.getUserId());
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "UPDATE Job_Application.M_D_User SET is_active=0, last_updated=GETDATE() WHERE user_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    	
    public int getTotalUsersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Job_Application.M_D_User WHERE is_active=1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    public void updateResumePath(int userId, String resumePath) throws SQLException {
        String sql = "UPDATE Job_Application.M_D_User SET resume = ?, last_updated = GETDATE() WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, resumePath);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
    
    
    
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM Job_Application.M_D_User WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}