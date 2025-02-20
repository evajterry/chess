package service;
import dataaccess.UserAccess;
import handlers.exception.*;
import model.UserData;

public class UserService {
    private final UserAccess userAccess;

    public UserService(UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    public void deleteAllData() throws ResponseException {
        userAccess.deleteAllData();
    }

    public Object registerUser(UserData user) throws ResponseException {
        return userAccess.registerUser(user);
    }
}