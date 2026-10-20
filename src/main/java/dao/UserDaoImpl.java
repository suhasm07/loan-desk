package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.User;

public class UserDaoImpl implements UserDao {
	private final String TABLE_NAME = "users";

	public UserDaoImpl() {
	}

	@Override
	public void setup() throws SQLException {
		try (Connection connection = Database.getConnection();
				Statement stmt = connection.createStatement();) {
			// COLLATE NOCASE makes Alex_1 and alex_1 the same username
			String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
					+ "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "username TEXT NOT NULL UNIQUE COLLATE NOCASE, "
					+ "password TEXT NOT NULL, "
					+ "first_name TEXT NOT NULL DEFAULT '', "
					+ "last_name TEXT NOT NULL DEFAULT '')";
			stmt.executeUpdate(sql);
		} 
	}

	@Override
	public User getUser(String username, String password) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND password = ?";
		try (Connection connection = Database.getConnection(); 
				PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setString(1, username);
			stmt.setString(2, password);
			
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					int userId = rs.getInt("user_id");
					String foundUsername = rs.getString("username");
					String firstName = rs.getString("first_name");
					String lastName = rs.getString("last_name");
					return new User(userId, foundUsername, firstName, lastName);
				}
				return null;
			} 
		}
	}

	@Override
	public boolean usernameExists(String username) throws SQLException {
		String sql = "SELECT user_id FROM " + TABLE_NAME + " WHERE username = ?";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setString(1, username);

			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	@Override
	public User createUser(String username, String password, String firstName, String lastName)
			throws SQLException {
		// Reject a duplicate username (not case sensitive)
		if (usernameExists(username)) {
			return null;
		}

		String sql = "INSERT INTO " + TABLE_NAME + " (username, password, first_name, last_name) "
				+ "VALUES (?, ?, ?, ?)";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, username);
			stmt.setString(2, password);
			stmt.setString(3, firstName);
			stmt.setString(4, lastName);
			stmt.executeUpdate();

			// Read back the ID that database generated
			try (ResultSet keys = stmt.getGeneratedKeys()) {
				if (keys.next()) {
					return new User(keys.getInt(1), username, firstName, lastName);
				}
				return null;
			}
		}
	}
}
