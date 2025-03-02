package service;
import dataaccess.AuthAccess;
import handlers.exception.*;
import model.AuthData;

public class AuthService {
    private final AuthAccess authAccess;

    public AuthService(AuthAccess authAccess) {
        this.authAccess = authAccess;
    }

    public void deleteAllData() throws ResponseException {
        authAccess.deleteAllData();
    }

    public void addAuthToken(String authToken) throws ResponseException {
        authAccess.insertAuthToken(authToken);
    }
}