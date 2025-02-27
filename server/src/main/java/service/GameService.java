package service;
import dataaccess.GameAccess;
import dataaccess.UserAccess;
import handlers.exception.*;
import model.UserData;
import spark.Response;

public class GameService {
    private final GameAccess gameAccess;
    private final UserAccess userAccess;

    public GameService(GameAccess gameAccess, UserAccess userAccess) {
        this.gameAccess = gameAccess;
        this.userAccess = userAccess;
    }

    public void deleteAllData() throws ResponseException {
        gameAccess.deleteAllData();
    }

    public Object createNewGame(String authToken, String gameName) throws ResponseException {
        // reasons to throw errors: (1) authToken not valid (2) gameName invalid?
        if (!isValidAuthToken(authToken)) {
            System.out.print(authToken);
            throw new ResponseException(401, "Error: authToken issue");
        }
        if (!isValidGameRequest(gameName)) {
            throw new ResponseException(400, "Error: bad request");
        }
        return gameAccess.createNewGame(authToken, gameName);
    }

    private boolean isValidGameRequest(String gameName) {
        return (gameName != null);
    }

    private boolean isValidAuthToken(String authToken) {
        return userAccess.userLoggedIn(authToken);
    }
}