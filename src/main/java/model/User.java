package model;

public class User {
	private int userId;
	private String firstName;
	private String lastName;
	private String username;
	private String password;

	public User() {
	}
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public User(int userId, String firstname, String lastname, String username) {
		this.userId = userId;
		this.firstName = firstname;
		this.lastName = lastname;
		this.username = username;
	}

	// getters and setters
	public int getUserId() {
		return userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	// returns full name for the welcome message and export header
	public String getFullName() {
		return firstName + " " + lastName;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
