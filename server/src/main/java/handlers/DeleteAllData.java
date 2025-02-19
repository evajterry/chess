package handlers;

import spark.Request;
import spark.Response;
import handlers.exception.*;
import service.*;

public class DeleteAllData {
    private AuthService authService;
    private UserService userService;
    private GameService gameService;

    public DeleteAllData(AuthService authService) {
        this.authService = authService;
        this.userService = userService;
        this.gameService = gameService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        userService.deleteAllData();
        authService.deleteAllData();
        gameService.deleteAllData();
        res.status(204);
        return "";
    }
}
