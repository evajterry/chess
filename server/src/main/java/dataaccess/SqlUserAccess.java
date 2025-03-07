package dataaccess;

import handlers.exception.ResponseException;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

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

    public String registerUser(UserData u) throws DataAccessException, SQLException {
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
        String username = u.username();
        addAuthDataHelper(newAuthToken, username);
        return newAuthToken;
    }

    private void addAuthDataHelper(String authToken, String username) throws SQLException, DataAccessException {
        String authUserDataQuery = "INSERT INTO AuthData (authToken, username, json) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(authUserDataQuery);
            ps.setString(1, authToken);
            ps.setString(2, username);
            ps.setString(3, "{}");
            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("Error registering user");
        }
    }

    public void deleteAllData() throws ResponseException {
        var statement = "TRUNCATE AuthData"; // might not need to do this bc AuthAccess deletes AuthData ?
        var statement2 = "TRUNCATE UserData";
        configuration.executeUpdate(statement2);
        configuration.executeUpdate(statement);
    }

    public String loginUser(UserData u) {
        String username = u.username();
        String queryString = "SELECT * FROM UserData WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(queryString)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (isCorrectPassword(u)) {
                        String newAuthToken = AuthAccess.createAuthToken();
                        addAuthDataHelper(newAuthToken, username);
                        return newAuthToken;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        return "Error: unauthorized";
    }

    public Boolean isCorrectPassword(UserData user) {
        String enteredPassword = user.password();
        String username = user.username();
        String queryString = "SELECT password FROM UserData WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(queryString)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String correctPassword = rs.getString("password");
                    return Objects.equals(enteredPassword, correctPassword);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
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
