package handlers;

import com.google.gson.Gson;
import exception.ErrorResponse;
import exception.ResponseException;
import spark.Request;
import spark.Response;
import service.*;
import java.util.List;
import java.util.Map;

public class ListGames {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListGames(GameService gameService) {
        this.gameService = gameService;
    }

    public Object handle(Request req, Response res) throws ResponseException {
        try {
            String authToken = req.headers("Authorization");
            List<Map<String, Object>> games = gameService.listGames(authToken);
            res.status(200);

            return Serializer.listOfGames(games);

        } catch (ResponseException e) {
            res.status(e.statusCode());
            return gson.toJson(new ErrorResponse(e.getMessage()));
        }
    }
}
