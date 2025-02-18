package dataaccess;

import model.UserData;

public interface UserDAO {
    void clearAll();
    String getUser(UserData u);
    void insertUser(UserData u);
}
