package dataaccess;

import handlers.exception.ResponseException;

public interface AuthDAO {
    void deleteAllData() throws ResponseException;
}
