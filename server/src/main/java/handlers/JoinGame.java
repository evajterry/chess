package handlers;

import com.google.gson.Gson;
import model.GameData;
import model.UserData;
import spark.Request;
import spark.Response;
import handlers.exception.*;
import service.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JoinGame {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGame(GameService gameService) {
        this.gameService = gameService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        try {
            String authToken = req.headers("Authorization");
            var gameData = new Gson().fromJson(req.body(), GameData.class);
            var user = new Gson().fromJson(req.body(), UserData.class);
            String username = user.username();
            int gameID = gameData.gameID();

            MyRequestBody body = gson.fromJson(req.body(), MyRequestBody.class);
            String requestedTeam = body.playerColor;

            gameService.joinNewGame(authToken, username, gameID, requestedTeam);
            res.status(200);

            return "{}";//Serializer.newGameCreated(gameID);

        } catch (ResponseException e) {
            res.status(e.StatusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}

class MyRequestBody {
    String playerColor;
}
