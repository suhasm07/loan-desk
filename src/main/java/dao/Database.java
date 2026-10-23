package dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	// Relative path, so it works on any computer
	private static final String DB_FOLDER = "data";
	private static final String DB_FILE = "loandesk.db";
	private static final String DEFAULT_URL = "jdbc:sqlite:" + DB_FOLDER + "/" + DB_FILE;

	// The tests change this to a temporary file. The normal app always uses the default.
	private static String databaseUrl = DEFAULT_URL;

	// Only the tests call this
	public static void useTestDatabase(String testUrl) {
		databaseUrl = testUrl;
	}

	// Puts things back to the normal database
	public static void useDefaultDatabase() {
		databaseUrl = DEFAULT_URL;
	}

	// Makes the data folder if it does not exist yet. An existing folder is left alone.
	private static void createFolder() throws SQLException {
		try {
			Files.createDirectories(Paths.get(DB_FOLDER));
		} catch (IOException e) {
			throw new SQLException("Cannot create the data folder: " + e.getMessage());
		}
	}

	public static Connection getConnection() throws SQLException {
		// The data folder is only needed for the normal database
		if (databaseUrl.equals(DEFAULT_URL)) {
			createFolder();
		}
		Connection connection = DriverManager.getConnection(databaseUrl);
		// SQLite only enforces foreign keys when this is switched on for each connection
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("PRAGMA foreign_keys = ON");
		}
		return connection;
	}
}