package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Objects;

public class UserAccess implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, String> usersAuthTokens = new HashMap<>(); // this might be the wrong way to do this
    // store authtoken username

    public String getUser(UserData u) {
        return u.username();
    }

    public void deleteAllData() {
        users.clear();
        usersAuthTokens.clear();
    }

    public boolean userLoggedIn(String authToken) {
        return usersAuthTokens.containsKey(authToken);
    }

    public void logoutUser(String authToken) {
        // initialize variable here
        usersAuthTokens.remove(authToken);
    }

    public String registerUser(UserData u) {
        UserData newUser = new UserData(u.username(), u.email(), u.password());
        users.put(u.username(), newUser);
        String newAuthToken = AuthAccess.createAuthToken();
        addAuthData(u.username(), newAuthToken); // should I be adding authData here?

        return newAuthToken;
    }

    public String loginUser(UserData u) {
//        UserData user = new UserData(u.username(), u.email(), u.password());
        if (users.containsKey(u.username())) {
            if (isCorrectPassword(u)) {
                String newAuthToken = AuthAccess.createAuthToken();
                addAuthData(u.username(), newAuthToken);
                return newAuthToken;
            } else {
                return ("Error: incorrect password");
            }
        }
        return "Error: unauthorized";
    }

    public Boolean isCorrectPassword(UserData user) {
        UserData dataBaseUser = users.get(user.username());
        String correctPassword = dataBaseUser.password();
        return Objects.equals(user.password(), correctPassword);
    }

    private void addAuthData(String username, String newAuthToken) {
        usersAuthTokens.put(newAuthToken, username);
    }

    public Boolean userExists(UserData u) {
        return users.containsKey(u.username());
    }

    public String getUsernameFromAuthToken(String authToken) {
        return usersAuthTokens.get(authToken); // No need for iteration
    }
}
