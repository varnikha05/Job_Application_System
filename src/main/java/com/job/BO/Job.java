package com.job.BO;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
public class Job {
	  private int jobId;
	    private int recruiterId;
	    private String title;
	    private String description;
	    private String location;
	    private String experience;
	    private String salary;
	    private String skills;
	    private String status;
	    private String jobType;
	    private String companyName;
	    private String companyWebsite;
	    private String companyDescription;
	    private LocalDateTime postedDate;
	    private LocalDate closingDate;
	    private LocalDateTime createdDate;
	    private LocalDateTime lastUpdated;
	    private boolean active;
	    private int applicationCount;
	    private String email;
	    private String recruiterEmail;
	    

	    public String getRecruiterEmail() {
			return recruiterEmail;
		}

		public void setRecruiterEmail(String recruiterEmail) {
			this.recruiterEmail = recruiterEmail;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public int getJobId() {
	        return jobId;
	    }

	    public void setJobId(int jobId) {
	        this.jobId = jobId;
	    }

	    public int getRecruiterId() {
	        return recruiterId;
	    }

	    public void setRecruiterId(int recruiterId) {
	        this.recruiterId = recruiterId;
	    }

	    public String getTitle() {
	        return title;
	    }

	    public void setTitle(String title) {
	        this.title = title;
	    }

	    public String getDescription() {
	        return description;
	    }

	    public void setDescription(String description) {
	        this.description = description;
	    }

	    public String getLocation() {
	        return location;
	    }

	    public void setLocation(String location) {
	        this.location = location;
	    }

	    public String getExperience() {
	        return experience;
	    }

	    public void setExperience(String experience) {
	        this.experience = experience;
	    }

	    public String getSalary() {
	        return salary;
	    }

	    public void setSalary(String salary) {
	        this.salary = salary;
	    }

	    public String getSkills() {
	        return skills;
	    }

	    public void setSkills(String skills) {
	        this.skills = skills;
	    }

	    public String getStatus() {
	        return status;
	    }

	    public void setStatus(String status) {
	        this.status = status;
	    }

	    public String getJobType() {
	        return jobType;
	    }

	    public void setJobType(String jobType) {
	        this.jobType = jobType;
	    }

	    public String getCompanyName() {
	        return companyName;
	    }

	    public void setCompanyName(String companyName) {
	        this.companyName = companyName;
	    }

	    public String getCompanyWebsite() {
	        return companyWebsite;
	    }

	    public void setCompanyWebsite(String companyWebsite) {
	        this.companyWebsite = companyWebsite;
	    }

	    public String getCompanyDescription() {
	        return companyDescription;
	    }

	    public void setCompanyDescription(String companyDescription) {
	        this.companyDescription = companyDescription;
	    }

	    public LocalDateTime getPostedDate() {
	        return postedDate;
	    }

	    public void setPostedDate(LocalDateTime postedDate) {
	        this.postedDate = postedDate;
	    }

	    public LocalDate getClosingDate() {
	        return closingDate;
	    }

	    public void setClosingDate(LocalDate closingDate) {
	        this.closingDate = closingDate;
	    }

	    public LocalDateTime getCreatedDate() {
	        return createdDate;
	    }

	    public void setCreatedDate(LocalDateTime createdDate) {
	        this.createdDate = createdDate;
	    }

	    public LocalDateTime getLastUpdated() {
	        return lastUpdated;
	    }

	    public void setLastUpdated(LocalDateTime lastUpdated) {
	        this.lastUpdated = lastUpdated;
	    }

	    public boolean isActive() {
	        return active;
	    }

	    public void setActive(boolean active) {
	        this.active = active;
	    }

	    public int getApplicationCount() {
	        return applicationCount;
	    }

	    public void setApplicationCount(int applicationCount) {
	        this.applicationCount = applicationCount;
	    }

    
    
 
  }

 