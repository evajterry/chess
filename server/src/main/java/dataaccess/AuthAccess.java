package dataaccess;

import model.AuthData;
import java.util.HashMap;

public class AuthAccess { // implements UserDAO
    private final HashMap<String, AuthData> auth = new HashMap<>();

    public void insertAuthToken(AuthData t) { //  throws DataAccessException
        AuthData newAuthToken = new AuthData(t.authToken(), t.username());
        auth.put(t.username(), newAuthToken);
    }
    public String getAuthToken(AuthData t) {
        return t.authToken();
    }

    public void deleteAllData() {
        auth.clear();
    }
}
