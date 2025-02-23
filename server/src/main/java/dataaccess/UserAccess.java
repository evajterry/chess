package dataaccess;

import model.UserData;
import java.util.HashMap;

public class UserAccess implements UserDAO{
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, String> usersAuthTokens = new HashMap<>(); // this might be the wrong way to do this

    public String getUser(UserData u) {
        return u.username();
    }

    @Override
    public void deleteAllData() {
        users.clear();
    }

    public String registerUser(UserData u) {
        UserData newUser = new UserData(u.username(), u.email(), u.password());
        users.put(u.username(), newUser);
        String newAuthToken = AuthAccess.createAuthToken();
        addAuthData(u.username(), newAuthToken);
        return newAuthToken;
    }

    private void addAuthData(String username, String newAuthToken) {
        usersAuthTokens.put(username, newAuthToken);
    }

    public Boolean userExists(UserData u) {
        return users.containsKey(u.username());
    }
}
