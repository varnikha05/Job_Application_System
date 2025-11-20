package com.job.BO;


public class User {
	private int userId;
    private String full_name;
    private String email;
    private String password;
    private String phone;
    private String gender;
    private String qualification;
    private String skills;
    private String resume;
    private String status;
  
    
    
	public User() {
		super();
		// TODO Auto-generated constructor stub
	}
	private byte[] resumeBytes;
	public byte[] getResumeBytes() { return resumeBytes; }
	public void setResumeBytes(byte[] resumeBytes) { this.resumeBytes = resumeBytes; }

	public String getFull_name() {
		return full_name;
	}
	public void setStatus(String status) {
	    this.status = status;
	}

	public String getStatus() {
	    return status;
	}

	public void setSkills1(String skills) {
	    this.skills = skills;
	}

	public String getSkills1() {
	    return skills;
	}

	public int getUserId() {
		return userId;
	}
	private String registered;

	public String getRegistered() {
	    return registered;
	}

	public void setRegistered(String registered) {
	    this.registered = registered;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}


	public void setFull_name(String full_name) {
		this.full_name = full_name;
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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}


	public void setActive(boolean boolean1) {
		// TODO Auto-generated method stub
		
	}


	public User(int userId, String full_name, String email, String password, String phone, String gender,
			String qualification, String skills, String resume) {
		super();
		this.userId = userId;
		this.full_name = full_name;
		this.email = email;
		this.password = password;
		this.phone = phone;
		this.gender = gender;
		this.qualification = qualification;
		this.skills = skills;
		this.resume = resume;
	}
	

//	@Override
//	public String toString() {
//		return "User [user_name=" + user_name + ", email=" + email + ", password=" + password + ", phone=" + phone
//				+ ", gender=" + gender + ", qualification=" + qualification + ", skills=" + skills + ", resume="
//				+ resume + "]";
//	}
//	
//	
	
	
}
   