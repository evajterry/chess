package dataaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthAccess implements AuthDAO { // implements UserDAO
    private final HashMap<String, String> auth = new HashMap<>(); // auth getting stored somewhere else?

    public static String createAuthToken() {
        return UUID.randomUUID().toString();
    }

    public void deleteAllData() {
        auth.clear();
    }
}
