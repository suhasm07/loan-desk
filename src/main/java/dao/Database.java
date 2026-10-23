package dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	private static final String URL_START = "jdbc:sqlite:";

	// Relative path, so it works on any computer
	private static final String NORMAL_URL = URL_START + "data/loandesk.db";

	// The database the application opens when it starts. Normally this is data/loandesk.db.
	// Starting the program with -Dloandesk.db=somefile.db opens that file instead,
	// and the normal database is not touched.
	private static final String START_URL = chooseStartUrl();

	// The tests and the demo program change this. The normal app uses START_URL.
	private static String databaseUrl = START_URL;

	private static String chooseStartUrl() {
		String customPath = System.getProperty("loandesk.db");
		if (customPath == null || customPath.trim().isEmpty()) {
			return NORMAL_URL;
		}
		return URL_START + customPath.trim();
	}

	// Used by the tests and by SeedDemoData
	public static void useTestDatabase(String testUrl) {
		databaseUrl = testUrl;
	}

	// Puts things back to the database the app started with
	public static void useDefaultDatabase() {
		databaseUrl = START_URL;
	}

	// Makes the folder of the database file if it does not exist yet.
	// An existing folder and an existing database file are left alone.
	private static void createFolder() throws SQLException {
		String filePath = databaseUrl.substring(URL_START.length());
		if (filePath.startsWith(":memory:")) {
			return;
		}
		try {
			Path folder = Paths.get(filePath).toAbsolutePath().getParent();
			if (folder != null) {
				Files.createDirectories(folder);
			}
		} catch (IOException e) {
			throw new SQLException("Cannot create the database folder: " + e.getMessage());
		}
	}

	public static Connection getConnection() throws SQLException {
		createFolder();
		Connection connection = DriverManager.getConnection(databaseUrl);
		// SQLite only enforces foreign keys when this is switched on for each connection
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("PRAGMA foreign_keys = ON");
		}
		return connection;
	}
}