package dataaccess;

import model.UserData;

public interface UserDAO {
    void deleteAllData();
    String registerUser(UserData user);
    String loginUser(UserData user);
}

//    String getUser(UserData u);
//    void insertUser(UserData u);
