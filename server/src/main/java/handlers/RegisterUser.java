package handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import exception.ErrorResponse;
import exception.ResponseException;
import spark.Request;
import spark.Response;
import service.*;
import model.UserData;

import java.sql.SQLException;

public class RegisterUser {
    private final UserService userService;
    private final AuthService authService;
    private final Gson gson = new Gson();

    public RegisterUser(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        try {
            var user = new Gson().fromJson(req.body(), UserData.class);
            String authToken = (String) userService.registerUser(user);
//            authService.addAuthToken(authToken);
            res.status(200);
            return Serializer.registeredUser(user, authToken);

        } catch (ResponseException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
