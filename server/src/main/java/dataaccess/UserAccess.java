package dataaccess;

import model.UserData;
import java.util.HashMap;

public class UserAccess implements UserDAO{
    private final HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void insertUser(UserData u) { //  throws DataAccessException
        UserData newUser = new UserData(u.username(), u.email(), u.password());
        users.put(u.username(), newUser);
    }
    @Override
    public String getUser(UserData u) {
        return u.username();
    }
    @Override
    public void clearAll() {
        users.clear();
    }
}
