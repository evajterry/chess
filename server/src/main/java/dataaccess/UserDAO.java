package dataaccess;

import handlers.exception.ResponseException;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface UserDAO {
    void deleteAllData() throws ResponseException;
    String registerUser(UserData user) throws DataAccessException, SQLException;
    String loginUser(UserData user);
    void logoutUser(String authToken) throws DataAccessException;
    Boolean isCorrectPassword(UserData user);
    Boolean userExists(UserData user);


}

//    String getUser(UserData u);
//    void insertUser(UserData u);
