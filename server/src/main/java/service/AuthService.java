package service;

import model.AuthData;
import model.UserData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {
    private final Map<String, AuthData> authTokens = new HashMap<>();
    public String generateAuthToken() {
        String token = UUID.randomUUID().toString();
        UserData username; // this is not how this works
//        authTokens.put(token, new AuthData(token, username));
        return token;
    }
}
