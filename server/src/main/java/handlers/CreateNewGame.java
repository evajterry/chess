package handlers;

import com.google.gson.Gson;
import model.GameData;
import spark.Request;
import spark.Response;
import handlers.exception.*;
import service.*;

public class CreateNewGame {
    private final GameService gameService;
    private final AuthService authService;
    private final Gson gson = new Gson();

    public CreateNewGame(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        try {
            String authToken = req.headers("Authorization");
            var gameData = new Gson().fromJson(req.body(), GameData.class);
            String gameName = gameData.gameName();

            //System.out.print(gameName); // gameName working
            String gameID = (String) gameService.createNewGame(authToken, gameName);
            res.status(200);

            return Serializer.newGameCreated(gameID);

        } catch (ResponseException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
