package client;

import client.APIClients.CreateGameRequest;
import client.APIClients.JoinGameRequest;
import com.google.gson.Gson;
import handlers.exception.ResponseException;
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
        return this.makeRequest("POST", path, user, AuthData.class);
    }

    public JoinGameRequest joinGame(String playerColor, String gameID) throws ResponseException {
        var path = "/game";
        int intGameID = Integer.parseInt(gameID);
        JoinGameRequest requestBody = new JoinGameRequest(intGameID, playerColor);
        return this.makeRequest("PUT", path, requestBody, JoinGameRequest.class); // null value
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
                System.out.println("Set request property: " + authToken);
            }
            System.out.print("AuthToken: ");
            System.out.println(authToken);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
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
            System.out.print("REQDATA: ");
            System.out.println(reqData);
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
                System.out.print("READER: ");
                System.out.print(reader);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                    System.out.print(response);
                }
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        System.out.print("STATUS: ");
        System.out.println(status);
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
