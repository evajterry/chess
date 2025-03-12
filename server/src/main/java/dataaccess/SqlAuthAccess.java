package dataaccess;

import com.google.gson.Gson;
import handlers.exception.ResponseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SqlAuthAccess implements AuthDAO {
    private final DBConfig configuration;

    public SqlAuthAccess() throws ResponseException, DataAccessException {
        this.configuration = new DBConfig();
        configuration.configureDatabase(createStatements);
    }

    private final HashMap<String, String> auth = new HashMap<>(); // auth getting stored somewhere else?

    public void insertAuthToken(String authToken) throws ResponseException, DataAccessException {
        var statement = "INSERT INTO AuthData (authToken, username, json) VALUES (?, ?, ?)";
        var json = new Gson().toJson(authToken);
        var username = getUserName(authToken);
        if (username == null) {
            throw new DataAccessException("Username not found for authToken: " + authToken);
        }
        configuration.executeUpdate(statement, authToken, username, json);
    }

    public boolean userLoggedIn(String authToken) {
        String query = "SELECT * FROM AuthData WHERE authToken = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUserName(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM AuthData WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String createAuthToken() {
        return UUID.randomUUID().toString();
    }

    public void deleteAllData() throws ResponseException {
        var statement = "TRUNCATE AuthData";
        configuration.executeUpdate(statement);
    }


    private final String[] createStatements = {
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
            """
    };

}
