package handlers;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
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
    public static Object listOfGames(List<Map<String, Object>> games) {
        Map<String, List<Map<String, Object>>> jsonObject = new HashMap<>();
        jsonObject.put("games", games);
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }
}
