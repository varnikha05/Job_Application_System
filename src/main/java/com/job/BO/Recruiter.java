package com.job.BO;

public class Recruiter {
	
	private int recruiterId;
	private String recruiter_name;
    private String companyName;
    private String email;
    private String password;
    private String phone;
    private String designation;
    private int status;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getRecruiter_name() {
		return recruiter_name;
	}
	public void setRecruiter_name(String recruiter_name) {
		this.recruiter_name = recruiter_name;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}
	public int getRecruiterId() { 
		return recruiterId;
		}
	public void setRecruiterId(int recruiterId) {
		this.recruiterId = recruiterId;
		}
	
    

}
