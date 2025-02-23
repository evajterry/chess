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

    public Object registerUser(UserData user) throws ResponseException {
        if (alreadyRegistered(user)) {
            throw new ResponseException(403, "Error: already taken"); // here's probably where I'd throw an error
        }
        if (!isValidUser(user)) {
            throw new ResponseException(400, "Error: bad request");
        }
        return userAccess.registerUser(user);
    }

    private Boolean isValidUser(UserData user) {
        return user.username() != null && user.password() != null && user.email() != null;
    }

    public Boolean alreadyRegistered(UserData user) {
        return userAccess.userExists(user);
    }
}