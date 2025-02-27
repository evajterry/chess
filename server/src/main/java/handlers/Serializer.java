package handlers;
import model.*;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Serializer {
    public static Object registeredUser(UserData user, String authToken) {
        Map<String, String> jsonObject = new HashMap<>();
        jsonObject.put("username", user.username());
        jsonObject.put("authToken", authToken);
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }
    public static Object newGameCreated(String gameID) {
        Map<String, String> jsonObject = new HashMap<>();
        jsonObject.put("gameID", gameID);
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }
}
