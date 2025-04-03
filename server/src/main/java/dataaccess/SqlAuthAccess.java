package dataaccess;

import exception.ResponseException;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;


public class SqlAuthAccess implements AuthDAO {
    private final DBConfig configuration;

    public SqlAuthAccess() throws ResponseException, DataAccessException {
        this.configuration = new DBConfig();
//        configuration.configureDatabase(createStatements);
    }

    private final HashMap<String, String> auth = new HashMap<>(); // auth getting stored somewhere else?

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

    public String getUsernameFromAuthToken(String authToken) {
        String query = "SELECT username FROM AuthData WHERE authToken = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, authToken);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database access error:", e);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String createAuthToken() {
        return UUID.randomUUID().toString();
    }

    public void deleteAllData() throws ResponseException {
        var statement = "TRUNCATE AuthData";
        configuration.executeUpdate(statement);
    }


}
