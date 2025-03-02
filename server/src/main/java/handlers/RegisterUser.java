package handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import handlers.exception.*;
import service.*;
import model.UserData;

public class RegisterUser {
    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterUser(UserService userService) {
        this.userService = userService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        try {
            var user = new Gson().fromJson(req.body(), UserData.class);
            String authToken = (String) userService.registerUser(user);
            res.status(200);
            return Serializer.registeredUser(user, authToken);

        } catch (ResponseException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
