package dataaccess;

import model.UserData;

public interface UserDAO {
    void deleteAllData();
    UserData registerUser(UserData user);

}

//    String getUser(UserData u);
//    void insertUser(UserData u);
