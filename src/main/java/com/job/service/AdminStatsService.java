package com.job.service;

import com.job.DAO.AdminStatsDao;

public class AdminStatsService {
    private final AdminStatsDao dao = new AdminStatsDao();
 
    public int getTotalUsers() throws Exception { 
    	return dao.countUsers(); 
    	}
    public int getTotalRecruiters() throws Exception { 
    	return dao.countRecruiters();
    	}
    public int getTotalJobs() throws Exception {
    	return dao.countJobs(); 
    	}
    public int getTotalApplications() throws Exception { 
    	return dao.countApplications(); 
    	}
}