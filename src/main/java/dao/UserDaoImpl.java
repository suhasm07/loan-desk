package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import model.User;
import service.PasswordHasher;

public class UserDaoImpl implements UserDao {
	private static final String TABLE_NAME = "users";

	public UserDaoImpl() {
	}

	@Override
	public void setup() throws SQLException {
		try (Connection connection = Database.getConnection();
			 Statement stmt = connection.createStatement();) {
			// COLLATE NOCASE makes Alex_1 and alex_1 the same username.
			// The password is never stored. Only the hash, the salt and the iteration count are.
			String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
					+ "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "username TEXT NOT NULL UNIQUE COLLATE NOCASE, "
					+ "password_hash TEXT NOT NULL, "
					+ "salt TEXT NOT NULL, "
					+ "iterations INTEGER NOT NULL, "
					+ "first_name TEXT NOT NULL DEFAULT '', "
					+ "last_name TEXT NOT NULL DEFAULT '')";
			stmt.executeUpdate(sql);
		}
	}

	// Finds the user by username, then checks the typed password against the saved hash
	@Override
	public User getUser(String username, String password) throws SQLException {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ?";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setString(1, username);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				String savedHash = rs.getString("password_hash");
				String savedSalt = rs.getString("salt");
				int savedIterations = rs.getInt("iterations");

				boolean passwordIsRight = PasswordHasher.passwordMatches(password, savedSalt, savedIterations, savedHash);
				if (!passwordIsRight) {
					return null;
				}

				int userId = rs.getInt("user_id");
				String foundUsername = rs.getString("username");
				String firstName = rs.getString("first_name");
				String lastName = rs.getString("last_name");
				return new User(userId, foundUsername, firstName, lastName);
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

		// Every account gets its own new salt
		String salt = PasswordHasher.makeSalt();
		String passwordHash = PasswordHasher.hashPassword(password, salt);

		String sql = "INSERT INTO " + TABLE_NAME
				+ " (username, password_hash, salt, iterations, first_name, last_name) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {
			stmt.setString(1, username);
			stmt.setString(2, passwordHash);
			stmt.setString(3, salt);
			stmt.setInt(4, PasswordHasher.ITERATIONS);
			stmt.setString(5, firstName);
			stmt.setString(6, lastName);
			stmt.executeUpdate();

			// Read back the ID the database generated
			try (ResultSet keys = stmt.getGeneratedKeys()) {
				if (keys.next()) {
					return new User(keys.getInt(1), username, firstName, lastName);
				}
				return null;
			}
		}
	}

	// Changes the first and last name of one user.
	// The WHERE user_id = ? part means it can only change that user's own row.
	@Override
	public boolean updateName(int userId, String firstName, String lastName) throws SQLException {
		String sql = "UPDATE " + TABLE_NAME + " SET first_name = ?, last_name = ? WHERE user_id = ?";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setString(1, firstName);
			stmt.setString(2, lastName);
			stmt.setInt(3, userId);

			int rowsChanged = stmt.executeUpdate();
			return rowsChanged == 1;
		}
	}

	// Checks a typed password against the saved hash of one user
	@Override
	public boolean passwordMatches(int userId, String password) throws SQLException {
		String sql = "SELECT password_hash, salt, iterations FROM " + TABLE_NAME + " WHERE user_id = ?";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setInt(1, userId);

			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					return false;
				}
				String savedHash = rs.getString("password_hash");
				String savedSalt = rs.getString("salt");
				int savedIterations = rs.getInt("iterations");
				return PasswordHasher.passwordMatches(password, savedSalt, savedIterations, savedHash);
			}
		}
	}

	// Saves a new password for one user. A brand new salt is made every time.
	@Override
	public boolean updatePassword(int userId, String newPassword) throws SQLException {
		String newSalt = PasswordHasher.makeSalt();
		String newHash = PasswordHasher.hashPassword(newPassword, newSalt);

		String sql = "UPDATE " + TABLE_NAME + " SET password_hash = ?, salt = ?, iterations = ? WHERE user_id = ?";
		try (Connection connection = Database.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql);) {
			stmt.setString(1, newHash);
			stmt.setString(2, newSalt);
			stmt.setInt(3, PasswordHasher.ITERATIONS);
			stmt.setInt(4, userId);

			int rowsChanged = stmt.executeUpdate();
			return rowsChanged == 1;
		}
	}
}