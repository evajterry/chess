package handlers;

import com.google.gson.Gson;
import exception.ErrorResponse;
import exception.ResponseException;
import model.GameData;
import spark.Request;
import spark.Response;
import service.*;

public class CreateNewGame {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateNewGame(GameService gameService, AuthService authService) {
        this.gameService = gameService;
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
