package handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import handlers.exception.*;
import service.*;
import model.UserData;

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
            authService.addAuthToken(authToken);
            res.status(200);
            return Serializer.registeredUser(user, authToken);

        } catch (ResponseException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
