package dataaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthAccess { // implements UserDAO
    private final HashMap<String, String> auth = new HashMap<>(); // auth getting stored somewhere else?

    public void insertAuthToken(String authToken) { //  throws DataAccessException
        String username = getUserName(authToken);
        auth.put(authToken, username);
    }

    private String getUserName(String authToken) {
        String username = "";
        for (Map.Entry<String, String> entry : auth.entrySet()) {
            if (entry.getValue().equals(authToken)) {
                username = entry.getKey();
            }
        }
        return username;
    }

    public static String createAuthToken() {
        return UUID.randomUUID().toString();
    }

    public void deleteAllData() {
        auth.clear();
    }
}
