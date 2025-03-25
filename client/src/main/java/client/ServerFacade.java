package client;

import client.apiclients.CreateGameRequest;
import client.apiclients.JoinGameRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private  String authToken;

    public ServerFacade(String url) {
        serverUrl = url;
        this.authToken = null;
    }

    public int createNewGame(String gameName) throws ResponseException {
        var path = "/game";
        CreateGameRequest requestBody = new CreateGameRequest(gameName);
        GameData response = this.makeRequest("POST", path, requestBody, GameData.class);
        return response.gameID();
    }

    public AuthData login(UserData user) throws ResponseException {
        var path = "/session";
        AuthData authData = this.makeRequest("POST", path, user, AuthData.class);
        authToken = authData.authToken();
        return authData;
    }

    public AuthData register(UserData user) throws ResponseException {
        var path = "/user";
        AuthData authData = this.makeRequest("POST", path, user, AuthData.class);
        authToken = authData.authToken();
        return authData;
    }

    public JoinGameRequest joinGame(String playerColor, int gameID) throws ResponseException {
        var path = "/game";
        JoinGameRequest requestBody = new JoinGameRequest(gameID, playerColor);
        return this.makeRequest("PUT", path, requestBody, JoinGameRequest.class); // null value
    }

    public void observeGame(String gameID) throws ResponseException {
        // figure out a way to observe the specific game
        // figure out how to read in

        ui.ChessBoardUI.printChessBoard("WHITE");
    }

    public List<Map<String, Object>> listGames() throws ResponseException {
        var path = "/game"; // GET
        JsonObject responseObject = this.makeRequest("GET", path, null, JsonObject.class); // Expect JSON
        JsonArray gamesArray = responseObject.getAsJsonArray("games");
        return new Gson().fromJson(gamesArray, new TypeToken<List<Map<String, Object>>>(){}.getType());
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, authToken, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            // handle the header
            if (authToken != null && !authToken.isEmpty()) {
                http.setRequestProperty("Authorization", authToken);
            } // not updated here
            writeBody(request, http); // not updated here
            http.connect(); // not here
            throwIfNotSuccessful(http);
            return readBody(http, responseClass); // not here
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr, status);
                }
            }
            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
