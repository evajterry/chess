package dataaccess;

import exception.ResponseException;

public interface AuthDAO {
    void deleteAllData() throws ResponseException;
}
