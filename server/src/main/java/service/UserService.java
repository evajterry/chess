package service;
import dataaccess.UserAccess;
import handlers.exception.*;
import model.UserData;
import org.eclipse.jetty.client.api.Response;

public class UserService {
    private final UserAccess userAccess;

    public UserService(UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    public void deleteAllData() throws ResponseException {
        userAccess.deleteAllData();
    }

    public void logoutUser(String authToken) throws ResponseException {
        if (!isValidAuthToken(authToken)) {
            throw new ResponseException(401, "Error: not authorized");
        } else {
            userAccess.logoutUser(authToken);
        }
//        return userAccess.logoutUser(authToken);
    }

    private boolean isValidAuthToken(String authToken) {
        return userAccess.userLoggedIn(authToken);
    }

    public Object registerUser(UserData user) throws ResponseException {
        if (alreadyRegistered(user)) {
            throw new ResponseException(403, "Error: already taken"); // here's probably where I'd throw an error
        }
        if (!isValidUser(user)) {
            throw new ResponseException(400, "Error: bad request");
        }
        return userAccess.registerUser(user);
    }

    public Object loginUser(UserData user) throws ResponseException {
        if (userAccess.userExists(user)) {
            if (!userAccess.isCorrectPassword(user)) {
                throw new ResponseException(401, "Error: password incorrect");
            }
            return userAccess.loginUser(user);
        } else {
            throw new ResponseException(401, "Error: user does not exist");
        }
    }

    private Boolean isValidUser(UserData user) {
        return user.username() != null && user.password() != null && user.email() != null;
    }

    public Boolean alreadyRegistered(UserData user) {
        return userAccess.userExists(user);
    }
}