package service;
import dataaccess.AuthAccess;
import dataaccess.DataAccessException;
import dataaccess.SqlAuthAccess;
import handlers.exception.*;
import model.AuthData;

public class AuthService {
    private final SqlAuthAccess sqlAuthAccess;

    public AuthService(SqlAuthAccess sqlAuthAccess) {
        this.sqlAuthAccess = sqlAuthAccess;
    }

    public void deleteAllData() throws ResponseException {
        sqlAuthAccess.deleteAllData();
    }

    public void addAuthToken(String authToken) throws ResponseException, DataAccessException {
        sqlAuthAccess.insertAuthToken(authToken);
    }
}