package service;
import dataaccess.GameAccess;
import dataaccess.UserAccess;
import handlers.exception.*;
import model.GameData;
import model.UserData;
import spark.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public List<Map<String, Object>> listGames(String authToken) throws ResponseException {
        if (!isValidAuthToken(authToken)) {
            throw new ResponseException(401, "Error: authToken issue");
        }
        return gameAccess.listGames(authToken);
    }

    public void joinNewGame(String authToken, String username, int gameID, String reqTeam) throws ResponseException {
        if (!isValidAuthToken(authToken)) {
            throw new ResponseException(401, "Error: authToken issue");
        }
        if (!isValidGameIDRequest(gameID)) {
            throw new ResponseException(400, "Error: bad request");
        }
        if (reqTeam == null) {
            throw new ResponseException(400, "Error: need requested team color");
        } if (!acceptedTeamColor(reqTeam)) {
            throw new ResponseException(400, "Error: bad team color request");
        }else {
            gameAccess.joinNewGame(authToken, username, gameID, reqTeam);
        }
    }

    private boolean acceptedTeamColor(String reqTeam) {
        return Objects.equals(reqTeam, "WHITE") || Objects.equals(reqTeam, "BLACK");
    }

    private boolean isValidGameIDRequest(int gameID) {
        return (gameID > 0); // this might need to be changed
    }

    private boolean isValidGameRequest(String gameName) {
        return (gameName != null);
    }

    private boolean isValidAuthToken(String authToken) {
        return userAccess.userLoggedIn(authToken);
    }
}