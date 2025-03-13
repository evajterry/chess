package dataaccess;

import handlers.exception.ResponseException;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    throw new Exception("Unable to load db.properties");
                }
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }

            createTables();

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Creates the necessary tables in the database if they do not already exist.
     */
    static void createTables() throws DataAccessException {
        String GameData =
            """
                CREATE TABLE IF NOT EXISTS GameData (
                    `id` int NOT NULL AUTO_INCREMENT,
                    `whiteUsername` varchar(256),
                    `blackUsername` varchar(256),
                    `gameID` int NOT NULL,
                    `gameName` varchar(256),
                    `game` TEXT DEFAULT NULL, 
                    PRIMARY KEY (`id`),
                    INDEX(whiteUsername),
                    INDEX(blackUsername),
                    INDEX(gameID),
                    INDEX(gameName),
                    INDEX(game(255))
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
                """;

        String UserData =
                """
                CREATE TABLE IF NOT EXISTS UserData (
                  `id` int NOT NULL AUTO_INCREMENT,
                  `username` varchar(256) NOT NULL,
                  `email` varchar(256) NOT NULL,
                  `password` varchar(256) NOT NULL,
                  `json` TEXT DEFAULT NULL,
                  PRIMARY KEY (`id`),
                  INDEX (username),
                  INDEX (email)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                """;

        String AuthData =
                """
                CREATE TABLE IF NOT EXISTS AuthData (
                  `id` int NOT NULL AUTO_INCREMENT,
                  `authToken` varchar(256) NOT NULL,
                  `username` varchar(256) NOT NULL,
                  `json` TEXT DEFAULT NULL,
                  PRIMARY KEY (`id`),
                  INDEX(authToken),
                  INDEX(username)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                """;

        try (var conn = getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate(GameData);
            stmt.executeUpdate(UserData);
            stmt.executeUpdate(AuthData);

        } catch (SQLException e) {
            throw new DataAccessException("Error creating tables: " + e.getMessage());
        }
    }
}
