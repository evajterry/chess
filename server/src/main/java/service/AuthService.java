package service;

import dataaccess.SqlAuthAccess;
import exception.ResponseException;

public class AuthService {
    private final SqlAuthAccess sqlAuthAccess;

    public AuthService(SqlAuthAccess sqlAuthAccess) {
        this.sqlAuthAccess = sqlAuthAccess;
    }

    public void deleteAllData() throws ResponseException {
        sqlAuthAccess.deleteAllData();
    }
}