package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.UUID;

public class AuthAccess { // implements UserDAO
    private final HashMap<String, AuthData> auth = new HashMap<>();

    public void insertAuthToken(AuthData t) { //  throws DataAccessException
        AuthData newAuthToken = new AuthData(t.authToken(), t.username());
        auth.put(t.username(), newAuthToken);
    }
    public String getAuthToken(AuthData t) {
        return t.authToken();
    }

    public static String createAuthToken() {
        return UUID.randomUUID().toString();
    }

    public void deleteAllData() {
        auth.clear();
    }
}
