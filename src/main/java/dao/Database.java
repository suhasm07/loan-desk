package dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	// This is a relative path, so it works on any computer
	private static final String DB_FOLDER = "data";
	private static final String DB_FILE = "loandesk.db";
	private static final String DB_URL = "jdbc:sqlite:" + DB_FOLDER + "/" + DB_FILE;

	// Makes the data folder if it does not exist yet. An existing folder is left alone.
	private static void createFolder() throws SQLException {
		try {
			Files.createDirectories(Paths.get(DB_FOLDER));
		} catch (IOException e) {
			throw new SQLException("Cannot create the data folder: " + e.getMessage());
		}
	}

	public static Connection getConnection() throws SQLException {
		createFolder();
		Connection connection = DriverManager.getConnection(DB_URL);
		// SQLite only enforces foreign keys when this is switched on for each connection
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("PRAGMA foreign_keys = ON");
		}
		return connection;
	}
}