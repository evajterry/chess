package dataaccess;

import model.UserData;
import java.util.HashMap;

public class UserAccess implements UserDAO{
    private final HashMap<String, UserData> users = new HashMap<>();

    public String getUser(UserData u) {
        return u.username();
    }

    @Override
    public void deleteAllData() {
        users.clear();
    }

    public UserData registerUser(UserData u) {
        UserData newUser = new UserData(u.username(), u.email(), u.password());
        users.put(u.username(), newUser);
        return newUser;
    }
}
