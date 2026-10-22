package model;

public class User {
	private int userId;
	private String username;
	private String firstName;
	private String lastName;

	public User() {
	}

	// Used when loading a full user from the database
	public User(int userId, String username, String firstName, String lastName) {
		this.userId = userId;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public int getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	// Full name for the welcome message and export header
	public String getFullName() {
		return firstName + " " + lastName;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}