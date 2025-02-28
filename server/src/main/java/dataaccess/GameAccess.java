package dataaccess;

import chess.ChessGame;
import handlers.exception.ResponseException;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.*;

public class GameAccess {
    private final HashMap<Integer, GameData> game = new HashMap<>();
    private final ArrayList<Integer> idList = new ArrayList<>();
    private final UserAccess userAccess;

    public GameAccess(UserAccess userAccess) { // Constructor to inject UserAccess
        this.userAccess = userAccess;
    }

    public void deleteAllData() {
        game.clear();
    }

    public String createNewGame(String authToken, String gameName) {
        // GameData(String whiteUsername, String blackUsername, int gameID, String gameName, ChessGame game)
        if (isValidLogIn(authToken)) {
            int gameID = generateNewID();
            GameData newGame = new GameData(null, null, gameID, gameName, new ChessGame());
            game.put(gameID, newGame);
            return String.valueOf(gameID);
        } else {
            return "Error: unauthorized";
        }
    }

    public List<Map<String, Object>> listGames(String authToken) throws ResponseException {
        if (!isValidLogIn(authToken)) {
            throw new ResponseException(401, "Error: unauthorized");
        }
        List<Map<String, Object>> gamesList = new ArrayList<>();
        for (GameData gameData : game.values()) {
            String gameIDString = Integer.toString(gameData.gameID());
            String whiteUser = gameData.whiteUsername(); // maybe need to do : ? here
            String blackUser = gameData.blackUsername();
            String gameName = gameData.gameName();

            Map<String, Object> gameMap = new HashMap<>();
            gameMap.put("gameID", gameIDString);
            gameMap.put("whiteUsername", whiteUser);
            gameMap.put("blackUsername", blackUser);
            gameMap.put("gameName", gameName);
            gamesList.add(gameMap);
        }
        return gamesList;
            // game object : {"gameID": 1234, "whiteUsername":"", "blackUsername":"", "gameName:""}
    }

    public void joinNewGame(String authToken, String username, int gameID, String requestedTeam) {
        if (isValidLogIn(authToken) && checkGameIDExists(gameID)) {
            GameData targetGame = game.get(gameID);
            // should I check if the game is full? and should this function be void?
            //if (targetGame.blackUsername() == null )
            if (Objects.equals(requestedTeam, "WHITE/BLACK")) {
                if (targetGame.whiteUsername() == null) {
                    GameData updatedGame = new GameData(username, targetGame.blackUsername(), gameID, targetGame.gameName(), targetGame.game());
                    game.replace(gameID, updatedGame);
                } else if (targetGame.blackUsername() == null) {
                    GameData updatedGame = new GameData(targetGame.whiteUsername(), username, gameID, targetGame.gameName(), targetGame.game());
                    game.replace(gameID, updatedGame);
                }
            }
            else if (targetGame.blackUsername() == null && Objects.equals(requestedTeam, "BLACK")) {
                GameData updatedGame = new GameData(targetGame.whiteUsername(), username, gameID, targetGame.gameName(), targetGame.game());
                game.replace(gameID, updatedGame);
            }
            else if (targetGame.whiteUsername() == null && Objects.equals(requestedTeam, "WHITE")) {
                GameData updatedGame = new GameData(username, targetGame.blackUsername(), gameID, targetGame.gameName(), targetGame.game());
                game.replace(gameID, updatedGame);
            }
            else if (targetGame.blackUsername() == null) {
                GameData updatedGame = new GameData(targetGame.whiteUsername(), username, gameID, targetGame.gameName(), targetGame.game());
                game.replace(gameID, updatedGame);
            }
            else if (targetGame.whiteUsername() == null) {
                GameData updatedGame = new GameData(username, targetGame.blackUsername(), gameID, targetGame.gameName(), targetGame.game());
                game.replace(gameID, updatedGame);
            }
        }
    }

    private boolean checkGameIDExists(int gameID) {
        return(idList.contains(gameID));
    }

    private boolean isValidLogIn(String authToken) {
        return userAccess.userLoggedIn(authToken);
    }

    private int generateNewID() {
        if (idList.isEmpty()) {
            idList.add(1);
            return 1;
        } else {
            int lastID = idList.getLast();
            lastID++;
            idList.add(lastID);
            return lastID;
        }
    }
}
