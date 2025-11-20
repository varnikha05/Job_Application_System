package com.job.service;
 
import com.job.BO.JobApplication;
import com.job.DAO.JobApplicationDao;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
 
public class JobApplicationService {
 
    private final JobApplicationDao jobApplicationDao = new JobApplicationDao();
 
  
    public List<JobApplication> getApplicationsForRecruiter(int recruiterId, String statusFilter, String dateFilter, Integer limit) {
        try {
            return jobApplicationDao.getApplicationsForRecruiter(recruiterId);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
 
   
    public boolean hasUserAlreadyApplied(int jobId, int userId) {
        try {
            return jobApplicationDao.userHasActiveApplication(jobId, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // assume already applied in case of error
        }
    }
 
  
    public boolean applyForJob(int jobId, int userId) {
        try {
            return jobApplicationDao.applyJob(userId, jobId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 
   
    public boolean updateApplicationStatus(int applicationId, int recruiterId, String status, LocalDateTime interviewDate) {
        try {
            return jobApplicationDao.updateApplicationStatus(applicationId, recruiterId, status, interviewDate);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 

    public List<JobApplication> getApplicationsForUser(int userId) {
        try {
            return jobApplicationDao.getApplicationsForUser(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
 