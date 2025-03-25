package handlers;

import exception.ResponseException;
import spark.Request;
import spark.Response;
import service.*;

public class DeleteAllData {
    private final AuthService authService;
    private final UserService userService;
    private final GameService gameService;

    public DeleteAllData(AuthService authService, UserService userService, GameService gameService) {
        this.authService = authService;
        this.userService = userService;
        this.gameService = gameService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        userService.deleteAllData();
        authService.deleteAllData();
        gameService.deleteAllData();
        res.status(200);
        return "";
    }
}
