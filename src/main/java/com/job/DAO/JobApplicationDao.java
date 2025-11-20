package com.job.DAO;
 
import com.job.BO.JobApplication;
import com.job.db.DBConnection;
 
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
 
public class JobApplicationDao {
 
    // Check if a user already applied
    private static final String CHECK_EXISTING_APPLICATION =
            "SELECT COUNT(*) AS total FROM Job_Application.M_S_Job_Application WHERE job_id = ? AND user_id = ? AND is_active = 1";
 
    //Insert new job application
    private static final String INSERT_APPLICATION =
            "INSERT INTO Job_Application.M_S_Job_Application (job_id, recruiter_id, user_id, applied_date, status, last_updated, is_active) " +
                    "VALUES (?, ?, ?, GETDATE(), 'Applied', GETDATE(), 1)";
 
    
    private static final String UPDATE_STATUS =
            "UPDATE ja SET ja.status = ?, ja.interview_date = ?, ja.last_updated = GETDATE() " +
                    "FROM Job_Application.M_S_Job_Application ja " +
                    "INNER JOIN Job_Application.M_D_Job j ON ja.job_id = j.job_id " +
                    "WHERE ja.application_id = ? AND ja.is_active = 1 AND j.recruiter_id = ?";
 
    //  Get all applications by user
    private static final String SELECT_USER_APPLICATIONS =
            "SELECT ja.application_id, ja.job_id, ja.status, ja.applied_date, ja.interview_date, " +
                    "u.full_name AS applicant_name, u.email AS applicant_email, u.skills AS applicant_skills, u.resume, " +
                    "j.title AS job_title, j.location AS job_location, j.company_name " +
                    "FROM Job_Application.M_S_Job_Application ja " +
                    "JOIN Job_Application.M_D_Job j ON ja.job_id = j.job_id " +
                    "JOIN Job_Application.M_D_User u ON ja.user_id = u.user_id " +
                    "WHERE ja.is_active = 1 AND ja.user_id = ? " +
                    "ORDER BY ja.applied_date DESC";
 

    private static final String SELECT_RECRUITER_APPLICATIONS =
            "SELECT ja.application_id, ja.job_id, ja.applied_date, ja.status, ja.interview_date, " +
                    "u.full_name AS applicant_name, u.email AS applicant_email, u.skills AS applicant_skills, u.resume, " +
                    "j.title AS job_title, j.location AS job_location, j.company_name " +
                    "FROM Job_Application.M_S_Job_Application ja " +
                    "JOIN Job_Application.M_D_Job j ON ja.job_id = j.job_id " +
                    "JOIN Job_Application.M_D_User u ON ja.user_id = u.user_id " +
                    "WHERE ja.is_active = 1 AND j.recruiter_id = ? " +
                    "ORDER BY ja.applied_date DESC";
 
   
    public boolean applyJob(int userId, int jobId, String name, String email, String phone,
            String qualification, String skills, String location, String coverLetter,
            byte[] resumeBytes) throws SQLException {
		
		Connection conn = null;
		PreparedStatement checkStmt = null;
		PreparedStatement insertStmt = null;
		ResultSet rs = null;
		try {
		conn = DBConnection.getConnection();
		

		String checkSql = "SELECT COUNT(*) FROM Job_Application.M_S_Job_Application WHERE user_id = ? AND job_id = ?";
		checkStmt = conn.prepareStatement(checkSql);
		checkStmt.setInt(1, userId);
		checkStmt.setInt(2, jobId);
		rs = checkStmt.executeQuery();
		if (rs.next() && rs.getInt(1) > 0) {
		return false;
		}
		if (rs != null) { rs.close(); rs = null; }
		checkStmt.close();
		checkStmt = null;
		

		String insertSql =
		"INSERT INTO Job_Application.M_S_Job_Application " +
				"(job_id, recruiter_id, user_id, applied_date, status, skills, resume, created_date, last_updated, is_active) " +
				"SELECT ?, j.recruiter_id, ?, GETDATE(), 'Applied', ?, ?, GETDATE(), GETDATE(), 1 " +
				"FROM Job_Application.M_D_Job j WHERE j.job_id = ?";

		
		
		insertStmt = conn.prepareStatement(insertSql);
		insertStmt.setInt(1, jobId);
		insertStmt.setInt(2, userId);
		insertStmt.setString(3, skills);
		if (resumeBytes != null) {
		    insertStmt.setBytes(4, resumeBytes);
		} else {
		    insertStmt.setNull(4, java.sql.Types.VARBINARY);
		}

		insertStmt.setInt(5, jobId);
		
		int rows = insertStmt.executeUpdate();
		return rows > 0;
		} finally {
		if (rs != null) try { rs.close(); } catch (Exception ignored) {}
		if (checkStmt != null) try { checkStmt.close(); } catch (Exception ignored) {}
		if (insertStmt != null) try { insertStmt.close(); } catch (Exception ignored) {}
		if (conn != null) try { conn.close(); } catch (Exception ignored) {}
		}
		}


 

    private int getRecruiterIdForJob(Connection conn, int jobId) throws SQLException {
        String sql = "SELECT recruiter_id FROM Job_Application.M_D_Job WHERE job_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jobId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("recruiter_id");
        }
        return -1;
    }

    public List<JobApplication> getApplicationsForUser(int userId) throws SQLException {
        List<JobApplication> applications = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_USER_APPLICATIONS)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applications.add(mapRow(rs));
            }
        }
        return applications;
    }

    public List<JobApplication> getApplicationsForRecruiter(int recruiterId) throws SQLException {
        List<JobApplication> applications = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_RECRUITER_APPLICATIONS)) {
            stmt.setInt(1, recruiterId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                applications.add(mapRow(rs));
            }
        }
        return applications;
    }
 

    public boolean updateApplicationStatus(int applicationId, int recruiterId, String status, LocalDateTime interviewDate) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {
 
            stmt.setString(1, status);
            if (interviewDate != null)
                stmt.setTimestamp(2, Timestamp.valueOf(interviewDate));
            else
                stmt.setNull(2, Types.TIMESTAMP);
 
            stmt.setInt(3, applicationId);
            stmt.setInt(4, recruiterId);
 
            return stmt.executeUpdate() > 0;
        }
    }
    
 // Check if user has already applied for this job
    public boolean userHasActiveApplication(int jobId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Job_Application.M_S_Job_Application WHERE job_id = ? AND user_id = ? AND is_active = 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, jobId);
            statement.setInt(2, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        }
        return false;
    }
  
    private JobApplication mapRow(ResultSet rs) throws SQLException {
        JobApplication app = new JobApplication();
        app.setApplicationId(rs.getInt("application_id"));
        app.setJobId(rs.getInt("job_id"));
        app.setJobTitle(rs.getString("job_title"));
        app.setCompanyName(rs.getString("company_name"));
        app.setJobLocation(rs.getString("job_location"));
        app.setStatus(rs.getString("status"));
        app.setApplicantName(rs.getString("applicant_name"));
        app.setApplicantEmail(rs.getString("applicant_email"));
        app.setApplicantSkills(rs.getString("applicant_skills"));
        app.setResumePath(rs.getString("resume"));
 
        Timestamp applied = rs.getTimestamp("applied_date");
        if (applied != null) app.setAppliedDate(applied.toLocalDateTime());
 
        Timestamp interview = rs.getTimestamp("interview_date");
        if (interview != null) app.setInterviewDate(interview.toLocalDateTime());
 
        return app;
    }
}