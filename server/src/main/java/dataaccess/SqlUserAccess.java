package dataaccess;

import handlers.exception.ResponseException;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlUserAccess {
    private final DBConfig configuration;

    public SqlUserAccess() throws ResponseException, DataAccessException {
        this.configuration = new DBConfig();
        configuration.configureDatabase(createStatements);
    }
    public boolean userLoggedIn(String authToken) {
        return true;
    }

    public boolean userExists(UserData u) {
        String username = u.username();

        return true;
    }

    public String registerUser(UserData u) throws DataAccessException {
        UserData newUser = new UserData(u.username(), u.email(), u.password());
        String newAuthToken = AuthAccess.createAuthToken();
        String insertQuery = "INSERT INTO UserData (username, email, password, json) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(insertQuery);
            ps.setString(1, u.username());
            ps.setString(2, u.email());
            ps.setString(3, u.password()); // hash password
            ps.setString(4, "{}");
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DataAccessException("Username or email already exists");
            }
            throw new DataAccessException("Error registering user");
        }

        return newAuthToken;
    }

    private final String[] createStatements = {
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
            """
    };
}
