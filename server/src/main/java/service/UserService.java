package service;
import dataaccess.DataAccessException;
import dataaccess.SqlUserAccess;
import dataaccess.UserAccess;
import handlers.exception.*;
import model.UserData;
import org.eclipse.jetty.client.api.Response;

import java.sql.SQLException;

public class UserService {
    private final UserAccess userAccess;
    private final SqlUserAccess sqlUserAccess;

    public UserService(UserAccess userAccess, SqlUserAccess sqlUserAccess) throws ResponseException, DataAccessException {
        this.userAccess = userAccess;
        this.sqlUserAccess = new SqlUserAccess();
    }

    public void deleteAllData() throws ResponseException {
        sqlUserAccess.deleteAllData();
    }

    public void logoutUser(String authToken) throws ResponseException, DataAccessException {
        if (!isValidAuthToken(authToken)) {
            throw new ResponseException(401, "Error: not authorized");
        } else {
            sqlUserAccess.logoutUser(authToken);
        }
//        return userAccess.logoutUser(authToken);
    }

    private boolean isValidAuthToken(String authToken) throws DataAccessException {
        return sqlUserAccess.userLoggedIn(authToken);
    }

    public Object registerUser(UserData user) throws ResponseException, DataAccessException, SQLException {
        if (alreadyRegistered(user)) {
            throw new ResponseException(403, "Error: already taken"); // here's probably where I'd throw an error
        }
        if (!isValidUser(user)) {
            throw new ResponseException(400, "Error: bad request");
        }
        return sqlUserAccess.registerUser(user);
    }

    public Object loginUser(UserData user) throws ResponseException {
        if (sqlUserAccess.userExists(user)) {
            if (!sqlUserAccess.isCorrectPassword(user)) {
                throw new ResponseException(401, "Error: password incorrect");
            }
            return sqlUserAccess.loginUser(user);
        } else {
            throw new ResponseException(401, "Error: user does not exist");
        }
    }

    private Boolean isValidUser(UserData user) {
        return user.username() != null && user.password() != null && user.email() != null;
    }

    public Boolean alreadyRegistered(UserData user) {
        return sqlUserAccess.userExists(user);
    }
}