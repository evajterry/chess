package service;
import dataaccess.UserAccess;
import handlers.exception.*;

public class UserService {
    private final UserAccess userAccess;

    public UserService(UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    public void deleteAllData() throws ResponseException {
        userAccess.deleteAllData();
    }
}