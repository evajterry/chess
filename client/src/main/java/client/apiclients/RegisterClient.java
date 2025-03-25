package client.apiclients;
import com.google.gson.Gson;
import model.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class RegisterClient {

    private static final String SERVER_URL = "http://localhost:8081/user";

    public void register(UserData user) throws IOException {
        URL url = new URL(SERVER_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        String json = new Gson().toJson(user);
        try (OutputStream requestBody = connection.getOutputStream()) {
            requestBody.write(json.getBytes());
            requestBody.flush();
        }
        connection.connect();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                System.out.println("Registration successful!");
            }
        } else {
            try (InputStream errorBody = connection.getErrorStream()) {
                System.out.println("Error: " + new String(errorBody.readAllBytes()));
            }
        }
    }
}

