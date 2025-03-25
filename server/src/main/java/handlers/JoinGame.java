package handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import exception.ErrorResponse;
import exception.ResponseException;
import model.GameData;
import spark.Request;
import spark.Response;
import service.*;

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
            int gameID = gameData.gameID();

            MyRequestBody body = gson.fromJson(req.body(), MyRequestBody.class);
            String requestedTeam = body.playerColor;

            if (gameService.joinNewGame(authToken, gameID, requestedTeam)) {
                res.status(200);
            }
            return "{}";
        } catch (ResponseException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

class MyRequestBody {
    String playerColor;
}
