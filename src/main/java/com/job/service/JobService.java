package com.job.service;
 
import com.job.BO.Job;
import com.job.DAO.JobDAO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class JobService {
	 
    private final JobDAO jobDao = new JobDAO(); // use the DAO class you have
 
    public boolean createJob(Job job) {
        try {
            return jobDao.insertJob(job);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public List<Job> getActiveJobs(Integer limit) {
        try {
            return jobDao.getActiveJobs(limit);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    public List<Job> getActiveJobs(String title, String location) {
        try {
            return jobDao.getActiveJobs(title, location);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
     
 
    public Job getJobById(int jobId) {
        try {
            return jobDao.getJobById(jobId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    public int countJobsForRecruiter(int recruiterId) {
        try {
            return jobDao.countJobsByRecruiter(recruiterId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
 
    public List<Job> getJobsForRecruiter(int recruiterId, String statusFilter) {
        try {
            return jobDao.getJobsByRecruiter(recruiterId, statusFilter);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public int countActiveJobs() {
        try {
            return jobDao.countActiveJobs();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}