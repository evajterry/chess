package handlers;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Authentication;
import spark.Request;
import spark.Response;
import handlers.exception.*;
import service.*;
import model.UserData;

public class RegisterUser {
    private final UserService userService;

    public RegisterUser(UserService userService) {
        this.userService = userService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        userService.registerUser(user);
        res.status(204);
        return new Gson().toJson(user);
    }
}
