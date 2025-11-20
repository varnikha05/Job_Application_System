package com.job.service;

import com.job.BO.User;

import com.job.DAO.UserDao;

public class UserService {


	private UserDao userDao = new UserDao();
   
   public UserService()
   {
	   this.userDao= new UserDao();
   }

   public boolean register(User user) {
       return userDao.registerUser(user);
   }

   public User login(String email, String password) {
      return userDao.LoginUser(email, password);
   }
}



