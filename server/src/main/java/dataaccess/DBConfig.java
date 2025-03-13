package dataaccess;

import handlers.exception.ResponseException;

import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class DBConfig {
    public void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
    }

    public void deleteAllData() throws DataAccessException, ResponseException {
        String[] deleteStatements = {
                "DELETE FROM AuthTokens;",
                "DELETE FROM AuthData;",
                "DELETE FROM GameData;",
                "DELETE FROM UserData;"
        };

        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : deleteStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to delete data: %s", ex.getMessage()));
        }
//        configureDatabase(); // got rid of params
    }


    public int executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();
                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
