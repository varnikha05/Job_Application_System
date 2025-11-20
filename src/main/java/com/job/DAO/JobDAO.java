package com.job.DAO;
 
import com.job.BO.Job;
import com.job.db.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
public class JobDAO{
    //  method to merge new and old job data
    public Job mergeJobFields(Job newJob, Job oldJob) {
        Job merged = new Job();
        merged.setJobId(oldJob.getJobId());
        merged.setRecruiterId(oldJob.getRecruiterId());
        merged.setTitle((newJob.getTitle() != null && !newJob.getTitle().trim().isEmpty()) ? newJob.getTitle() : oldJob.getTitle());

        
        String newDesc = newJob.getDescription();
        if (newDesc == null || newDesc.trim().isEmpty() || "<p>No description provided</p>".equals(newDesc.trim())) {
            merged.setDescription(oldJob.getDescription());
        } else {
            merged.setDescription(newDesc);
        }

        merged.setLocation((newJob.getLocation() != null && !newJob.getLocation().trim().isEmpty()) ? newJob.getLocation() : oldJob.getLocation());
        merged.setExperience((newJob.getExperience() != null && !newJob.getExperience().trim().isEmpty()) ? newJob.getExperience() : oldJob.getExperience());
        merged.setSalary((newJob.getSalary() != null && !newJob.getSalary().trim().isEmpty()) ? newJob.getSalary() : oldJob.getSalary());

        String newSkills = newJob.getSkills();
        if (newSkills == null || newSkills.trim().isEmpty()) {
            merged.setSkills(oldJob.getSkills());
        } else {
            merged.setSkills(newSkills);
        }

        merged.setStatus((newJob.getStatus() != null && !newJob.getStatus().trim().isEmpty()) ? newJob.getStatus() : oldJob.getStatus());

   
        String newJobType = newJob.getJobType();
        if (newJobType == null || newJobType.trim().isEmpty()) {
            merged.setJobType(oldJob.getJobType());
        } else {
            merged.setJobType(newJobType);
        }

        merged.setCompanyName((newJob.getCompanyName() != null && !newJob.getCompanyName().trim().isEmpty()) ? newJob.getCompanyName() : oldJob.getCompanyName());
        merged.setCompanyWebsite((newJob.getCompanyWebsite() != null && !newJob.getCompanyWebsite().trim().isEmpty()) ? newJob.getCompanyWebsite() : oldJob.getCompanyWebsite());
        merged.setCompanyDescription((newJob.getCompanyDescription() != null && !newJob.getCompanyDescription().trim().isEmpty()) ? newJob.getCompanyDescription() : oldJob.getCompanyDescription());

        // Closing dat
        if (newJob.getClosingDate() != null) {
            merged.setClosingDate(newJob.getClosingDate());
        } else {
            merged.setClosingDate(oldJob.getClosingDate());
        }

        return merged;
    }
 
    // SQL query to insert a new job into your table
    private static final String INSERT_SQL =
        "INSERT INTO Job_Application.M_D_Job " +
        "(recruiter_id, title, description, location, experience, salary, skills, job_type, company_name, company_website, company_description, status, closing_date, last_updated) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
 
    // Method to insert a new job
    public boolean insertJob(Job job) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
 
            if (connection == null) {
               System.out.println("Database connection is null. Check DBConnection class.");
                return false;
            }
 
         
            ps.setInt(1, job.getRecruiterId());
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getDescription());
            ps.setString(4, job.getLocation());
            ps.setString(5, job.getExperience());
            ps.setString(6, job.getSalary());
            ps.setString(7, job.getSkills());
            ps.setString(8, job.getJobType());
            ps.setString(9, job.getCompanyName());
            ps.setString(10, job.getCompanyWebsite());
            ps.setString(11, job.getCompanyDescription());
            ps.setString(12, job.getStatus());
 
            LocalDate closingDate = job.getClosingDate();
            if (closingDate != null) {
                ps.setDate(13, java.sql.Date.valueOf(closingDate));
            } else {
                ps.setNull(13, java.sql.Types.DATE);
            }
 
            int rows = ps.executeUpdate();
            System.out.println("Job inserted successfully. Rows affected: " + rows);
            return rows > 0;
 
        } catch (SQLException e) {
            System.out.println("SQL Exception in insertJob: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("General Exception in insertJob: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
 
    // count total jobs for a recruiter
    public int countJobsByRecruiter(int recruiterId) {
        String sql = "SELECT COUNT(*) AS total FROM Job_Application.M_D_Job WHERE recruiter_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
 
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception in countJobsByRecruiter: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    //  get total job count 
    public int getTotalJobCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Job_Application.M_D_Job";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
 // maps each row from the database to a Job object
    private Job mapRow(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setJobId(rs.getInt("job_id"));
        job.setRecruiterId(rs.getInt("recruiter_id"));
        job.setTitle(rs.getString("title"));
        job.setDescription(rs.getString("description"));
        job.setLocation(rs.getString("location"));
        job.setExperience(rs.getString("experience"));
        job.setSalary(rs.getString("salary"));
        job.setSkills(rs.getString("skills"));
        job.setStatus(rs.getString("status"));
        job.setJobType(rs.getString("job_type"));
        job.setCompanyName(rs.getString("company_name"));
        job.setCompanyWebsite(rs.getString("company_website"));
        job.setCompanyDescription(rs.getString("company_description"));
        job.setClosingDate(rs.getDate("closing_date") == null ? null : rs.getDate("closing_date").toLocalDate());
        job.setPostedDate(rs.getTimestamp("posted_date") == null ? null : rs.getTimestamp("posted_date").toLocalDateTime());

        job.setActive(rs.getBoolean("is_active"));
        return job;
    }
     
    
    public boolean postJob(Job job) {
        String sql = "INSERT INTO Job_Application.M_D_Job " +
            "(recruiter_id, title, description, location, experience, salary, skills, job_type, company_name, company_website, company_description, closing_date, posted_date, created_date, last_updated, is_active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE(),GETDATE(), 1)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, job.getRecruiterId());
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getDescription());
            ps.setString(4, job.getLocation());
            ps.setString(5, job.getExperience());
            ps.setString(6, job.getSalary());
            ps.setString(7, job.getSkills());
            ps.setString(8, job.getJobType());
            ps.setString(9, job.getCompanyName());
            ps.setString(10, job.getCompanyWebsite());
            ps.setString(11, job.getCompanyDescription());

            if (job.getClosingDate() != null) {
                ps.setDate(12, java.sql.Date.valueOf(job.getClosingDate()));
            } else {
                ps.setNull(12, java.sql.Types.DATE);
            }

            int rows = ps.executeUpdate();
            System.out.println("Job posted successfully. Rows affected: " + rows);
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

     
    public Job getJobById(int jobId) {
        Job job = null;
        String query = "SELECT j.*, r.email AS email\r\n"
                + "FROM Job_Application.M_D_Job j\r\n"
                + "LEFT JOIN Job_Application.M_D_Recruiter r ON j.recruiter_id = r.recruiter_id\r\n"
                + "WHERE j.job_id = ?\r\n"
                + "";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, jobId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                job = new Job();
                job.setJobId(rs.getInt("job_id"));
                job.setRecruiterId(rs.getInt("recruiter_id"));
                job.setTitle(rs.getString("title"));
                job.setDescription(rs.getString("description"));
                job.setLocation(rs.getString("location"));
                job.setExperience(rs.getString("experience"));
                job.setSalary(rs.getString("salary"));
                job.setSkills(rs.getString("skills"));
                job.setStatus(rs.getString("status"));
                job.setJobType(rs.getString("job_type"));
                job.setCompanyName(rs.getString("company_name"));
                job.setCompanyWebsite(rs.getString("company_website"));
                job.setCompanyDescription(rs.getString("company_description"));
                job.setClosingDate(rs.getDate("closing_date") == null ? null : rs.getDate("closing_date").toLocalDate());
                job.setPostedDate(rs.getTimestamp("posted_date") == null ? null : rs.getTimestamp("posted_date").toLocalDateTime());

                
                job.setEmail(rs.getString("email")); // Add this new line
                
            
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return job;
    }

    
    
    public boolean updateJob(Job job) {
        String sql = "UPDATE Job_Application.M_D_Job SET " +
                "title=?, description=?, location=?, experience=?, salary=?, skills=?, " +
                "status=?, job_type=?, company_name=?, company_website=?, company_description=?, closing_date=?, last_updated=GETDATE() " +
                "WHERE job_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setString(3, job.getLocation());
            ps.setString(4, job.getExperience());
            ps.setString(5, job.getSalary());
            ps.setString(6, job.getSkills());
            ps.setString(7, job.getStatus());
            ps.setString(8, job.getJobType());
            ps.setString(9, job.getCompanyName());
            ps.setString(10, job.getCompanyWebsite());
            ps.setString(11, job.getCompanyDescription());

            if (job.getClosingDate() != null) {
                ps.setDate(12, java.sql.Date.valueOf(job.getClosingDate()));
            } else {
                ps.setNull(12, java.sql.Types.DATE);
            }

            ps.setInt(13, job.getJobId());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public List<Job> getActiveJobs(String title, String location) {
        List<Job> jobs = new ArrayList<>();
        StringBuilder query = new StringBuilder(
        "SELECT job_id, title, description, location, experience, salary, skills, company_name, status, posted_date " +
        "FROM Job_Application.M_D_Job WHERE is_active = 1 AND status = 'Active'"
        );
         
        if (title != null && !title.trim().isEmpty()) {  
            query.append(" AND title LIKE ?");  
        }  
        if (location != null && !location.trim().isEmpty()) {  
            query.append(" AND location LIKE ?");  
        }  
         
        query.append(" ORDER BY posted_date DESC");  
         
        try (Connection conn = DBConnection.getConnection();  
             PreparedStatement ps = conn.prepareStatement(query.toString())) {  
         
            int index = 1;  
            if (title != null && !title.trim().isEmpty()) {  
                ps.setString(index++, "%" + title + "%");  
            }  
            if (location != null && !location.trim().isEmpty()) {  
                ps.setString(index++, "%" + location + "%");  
            }  
         
            ResultSet rs = ps.executeQuery();  
            while (rs.next()) {  
                Job job = new Job();  
                job.setJobId(rs.getInt("job_id"));  
                job.setTitle(rs.getString("title"));  
                job.setDescription(rs.getString("description"));  
                job.setLocation(rs.getString("location"));  
                job.setExperience(rs.getString("experience"));  
                job.setSalary(rs.getString("salary"));  
                job.setSkills(rs.getString("skills"));  
                job.setCompanyName(rs.getString("company_name"));  
                job.setStatus(rs.getString("status"));  
                job.setPostedDate(rs.getTimestamp("posted_date").toLocalDateTime());  
                jobs.add(job);  
            }  
         
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
         
        return jobs;
         
        }
     
 
    public List<Job> getActiveJobs(Integer limit) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        String query = "SELECT TOP " + limit + " * FROM Job_Application.M_D_Job WHERE is_active = 1 ORDER BY posted_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                jobs.add(mapRow(rs));
            }
        }
        return jobs;
    }
    
    public int countActiveJobs() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM Job_Application.M_D_Job WHERE is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }
     
    public List<Job> getJobsByRecruiter(int recruiterId, String statusFilter) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM Job_Application.M_D_Job WHERE recruiter_id = ?");
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            query.append(" AND status = ?");
        }
        query.append(" ORDER BY posted_date DESC");
     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
     
            ps.setInt(1, recruiterId);
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                ps.setString(2, statusFilter);
            }
     
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapRow(rs));
                }
            }
        }
        return jobs;
    }
     

 // For user/public job listing
    public List<Job> getAllActiveJobs() {
        List<Job> jobList = new ArrayList<>();
        String sql =
        	    "SELECT j.*, r.email AS recruiter_email " +
        	    "FROM Job_Application.M_D_Job j " +
        	    "JOIN Job_Application.M_D_Recruiter r ON j.recruiter_id = r.recruiter_id " +
        	    "WHERE j.is_active = 1 " +
        	    "ORDER BY j.posted_date DESC";
     
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
     
            while (rs.next()) {
                Job job = new Job();
                job.setJobId(rs.getInt("job_id"));
                job.setTitle(rs.getString("title"));
                job.setDescription(rs.getString("description"));
                job.setLocation(rs.getString("location"));
                job.setExperience(rs.getString("experience"));
                job.setSalary(rs.getString("salary"));
                job.setSkills(rs.getString("skills"));
                job.setRecruiterEmail(rs.getString("recruiter_email"));
                jobList.add(job);
            }
     
        } catch (Exception e) {
            e.printStackTrace();
        }
     
        return jobList;
    }
     
    //For recruiter dashboard (jobs by recruiter)
    public List<Job> getJobsByRecruiter(int recruiterId) {
        List<Job> jobList = new ArrayList<>();
        String sql = "SELECT * FROM Job_Application.M_D_Job WHERE recruiter_id = ? AND is_active = 1 ORDER BY posted_date DESC";
     
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
     
            ps.setInt(1, recruiterId);
            ResultSet rs = ps.executeQuery();
     
            while (rs.next()) {
                Job job = new Job();
                job.setJobId(rs.getInt("job_id"));
                job.setTitle(rs.getString("title"));
                job.setDescription(rs.getString("description"));
                job.setLocation(rs.getString("location"));
                job.setExperience(rs.getString("experience"));
                job.setSalary(rs.getString("salary"));
                job.setSkills(rs.getString("skills"));
                job.setStatus(rs.getString("status"));
                jobList.add(job);
            }
     
        } catch (Exception e) {
            e.printStackTrace();
        }
     
        return jobList;
    } 
     
    
    public List<Job> searchJobs(String title, String location) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT job_id, title, location, company_name, status FROM Job_Application.M_D_Job WHERE is_active = 1"
        );
     
        if (title != null && !title.trim().isEmpty())
            sql.append(" AND title LIKE ?");
        if (location != null && !location.trim().isEmpty())
            sql.append(" AND location LIKE ?");
     
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
     
            int i = 1;
            if (title != null && !title.trim().isEmpty())
                ps.setString(i++, "%" + title + "%");
            if (location != null && !location.trim().isEmpty())
                ps.setString(i++, "%" + location + "%");
     
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Job job = new Job();
                job.setJobId(rs.getInt("job_id"));
                job.setTitle(rs.getString("title"));
                job.setLocation(rs.getString("location"));
                job.setCompanyName(rs.getString("company_name"));
                job.setStatus(rs.getString("status"));
                jobs.add(job);
            }
        }
        return jobs;
    }

    
    public List<Job> getPublicJobs(int limit, int offset, String title, String location, String jobType) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT job_id, title, description, company_name, location, salary, status, job_type " +
            "FROM Job_Application.M_D_Job WHERE is_active = 1"
        );

        if (title != null && !title.trim().isEmpty()) {
            sql.append(" AND title LIKE ?");
        }
        if (location != null && !location.trim().isEmpty()) {
            sql.append(" AND location LIKE ?");
        }
        if (jobType != null && !jobType.trim().isEmpty()) {
            sql.append(" AND job_type LIKE ?");
        }

        sql.append(" ORDER BY posted_date DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (title != null && !title.trim().isEmpty()) {
                ps.setString(index++, "%" + title + "%");
            }
            if (location != null && !location.trim().isEmpty()) {
                ps.setString(index++, "%" + location + "%");
            }
            if (jobType != null && !jobType.trim().isEmpty()) {
                ps.setString(index++, "%" + jobType + "%");
            }
            ps.setInt(index++, offset);
            ps.setInt(index, limit);

            System.out.println("Executing query: " + sql.toString());
            System.out.println("Parameters: title=" + title + ", location=" + location + ", jobType=" + jobType);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Job job = new Job();
                job.setJobId(rs.getInt("job_id"));
                job.setTitle(rs.getString("title"));
                job.setDescription(rs.getString("description"));
                job.setCompanyName(rs.getString("company_name"));
                job.setLocation(rs.getString("location"));
                job.setSalary(rs.getString("salary"));
                job.setStatus(rs.getString("status"));
                job.setJobType(rs.getString("job_type"));
                jobs.add(job);
            }
            System.out.println("Found " + jobs.size() + " jobs");
        }
        return jobs;
    }

    public int countPublicJobs(String title, String location, String jobType) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM Job_Application.M_D_Job WHERE is_active = 1"
        );

        if (title != null && !title.trim().isEmpty()) {
            sql.append(" AND title LIKE ?");
        }
        if (location != null && !location.trim().isEmpty()) {
            sql.append(" AND location LIKE ?");
        }
        if (jobType != null && !jobType.trim().isEmpty()) {
            sql.append(" AND job_type LIKE ?");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (title != null && !title.trim().isEmpty()) {
                ps.setString(index++, "%" + title + "%");
            }
            if (location != null && !location.trim().isEmpty()) {
                ps.setString(index++, "%" + location + "%");
            }
            if (jobType != null && !jobType.trim().isEmpty()) {
                ps.setString(index++, "%" + jobType + "%");
            }

            System.out.println("Counting with query: " + sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Total count: " + count);
                return count;
            }
        }
        return 0;
    }
    public int getFilledJobsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Job_Application.M_D_Job WHERE status = 'Closed' AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
 // Add this method to your JobDAO.java class
    public int getUniqueCompaniesCount() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT company_name) FROM Job_Application.M_D_Job WHERE is_active = 1 AND company_name IS NOT NULL AND company_name != ''";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
}