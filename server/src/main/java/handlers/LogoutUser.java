package handlers;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import handlers.exception.*;
import service.*;
import model.UserData;

public class LogoutUser {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LogoutUser(UserService userService) {
        this.userService = userService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        try {
            String authToken = req.headers("Authorization");
//            System.out.print(authToken);
            userService.logoutUser(authToken);
            res.status(200);

            return "{}";//Serializer.registeredUser(user, authToken); // update

        } catch (ResponseException e) {
            res.status(e.StatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
