package dataaccess;

import exception.ResponseException;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;


public class SqlUserAccess implements UserDAO {
    private final DBConfig configuration;

    public SqlUserAccess() throws ResponseException, DataAccessException {
        this.configuration = new DBConfig();
//        configuration.configureDatabase(createStatements);
    }


    public String registerUser(UserData u) throws DataAccessException, SQLException {
        String hashedPassword = BCrypt.hashpw(u.password(), BCrypt.gensalt());
        String newAuthToken = AuthAccess.createAuthToken();
        String insertQuery = "INSERT INTO UserData (username, email, password, json) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(insertQuery);
            ps.setString(1, u.username());
            ps.setString(2, u.email());
            ps.setString(3, hashedPassword); // hash password
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
//        var statement = "TRUNCATE AuthData"; // might not need to do this bc AuthAccess deletes AuthData ?
        var statement2 = "TRUNCATE UserData";
        configuration.executeUpdate(statement2);
//        configuration.executeUpdate(statement);
        // make sure delete all data is working
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
                    String hashedPassword = rs.getString("password");
                    return hashedPassword != null && BCrypt.checkpw(enteredPassword, hashedPassword);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public Boolean userExists(UserData u) {
        String username = u.username();
        String queryString = "SELECT username FROM UserData WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(queryString)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void logoutUser(String authToken) throws DataAccessException {
        String deleteQuery = "DELETE FROM AuthData WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
            ps.setString(1, authToken);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DataAccessException("Error: Invalid authToken or already logged out.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error logging out user.");
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean userLoggedIn(String authToken) throws DataAccessException {
        String query = "SELECT * FROM AuthData WHERE authToken = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking login status.");
        }
    }
}
