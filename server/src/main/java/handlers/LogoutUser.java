package handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import exception.ErrorResponse;
import exception.ResponseException;
import spark.Request;
import spark.Response;
import service.*;

public class LogoutUser {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LogoutUser(UserService userService) {
        this.userService = userService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        try {
            String authToken = req.headers("Authorization");
            userService.logoutUser(authToken);
            res.status(200);

            return "{}";//Serializer.registeredUser(user, authToken); // update

        } catch (ResponseException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
