package model;

import java.sql.SQLException;

import dao.LoanDao;
import dao.LoanDaoImpl;
import dao.UserDao;
import dao.UserDaoImpl;

public class Model {
	private UserDao userDao;
	private User currentUser;
	private LoanDao loanDao;
	
	public Model() {
		userDao = new UserDaoImpl();
		loanDao = new LoanDaoImpl();
	}

	// Makes the tables if they do not exist yet. The users table comes first because the loans table points to it.
	public void setup() throws SQLException {
		userDao.setup();
		loanDao.setup();
	}
	public UserDao getUserDao() {
		return userDao;
	}

	public LoanDao getLoanDao() {
		return loanDao;
	}
	
	public User getCurrentUser() {
		return this.currentUser;
	}
	
	public void setCurrentUser(User user) {
		currentUser = user;
	}
}
