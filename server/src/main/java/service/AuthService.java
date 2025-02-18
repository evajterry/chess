package service;
import dataaccess.AuthAccess;
import handlers.exception.*;

public class AuthService {
    private final AuthAccess authAccess;

    public AuthService(AuthAccess authAccess) {
        this.authAccess = authAccess;
    }

    public void deleteAllData() throws ResponseException {
        authAccess.deleteAllData();
    }
}