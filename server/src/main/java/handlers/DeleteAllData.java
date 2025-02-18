package handlers;

import spark.Request;
import spark.Response;
import handlers.exception.*;
import service.*;

public class DeleteAllData {
    private AuthService authService;
    private UserService userService;

    public DeleteAllData(AuthService authService) {
        this.authService = authService;
        this.userService = userService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        userService.deleteAllData();
        authService.deleteAllData();
        res.status(204);
        return "";
    }
}
