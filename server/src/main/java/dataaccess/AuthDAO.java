package dataaccess;

import handlers.exception.ResponseException;

public interface AuthDAO {
    void insertAuthToken(String authToken) throws ResponseException, DataAccessException;
    void deleteAllData() throws ResponseException;
}
