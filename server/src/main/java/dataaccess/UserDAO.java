package dataaccess;

import handlers.exception.ResponseException;
import model.GameData;
import model.UserData;

import java.util.List;
import java.util.Map;

public interface UserDAO {
    void deleteAllData();
    String registerUser(UserData user);
    String loginUser(UserData user);
    void logoutUser(String authToken);
}

//    String getUser(UserData u);
//    void insertUser(UserData u);
