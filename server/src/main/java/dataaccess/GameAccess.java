package dataaccess;

import chess.ChessGame;
import handlers.exception.ResponseException;
import model.GameData;

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
        idList.clear();
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
    }

    public boolean joinNewGame(String authToken, int gameID, String requestedTeam) {
        String username = getUsername(authToken);
        if (isValidLogIn(authToken) && checkGameIDExists(gameID)) {
            GameData targetGame = game.get(gameID);

            if (!isTeamReqTaken(targetGame, requestedTeam)) {
                GameData updatedGame = targetGame;
                if (Objects.equals(requestedTeam, "WHITE/BLACK")) {
                    if (targetGame.whiteUsername() == null) {
                        updatedGame = updatedGame.updateWhiteUsername(username);
                    } else if (targetGame.blackUsername() == null) {
                        updatedGame = updatedGame.updateBlackUsername(username);
                    }
                }
                else if (targetGame.blackUsername() == null && Objects.equals(requestedTeam, "BLACK")) {
                    updatedGame = updatedGame.updateBlackUsername(username);
                }
                else if (targetGame.whiteUsername() == null && Objects.equals(requestedTeam, "WHITE")) {
                    updatedGame = updatedGame.updateWhiteUsername(username);
                }
                else if (targetGame.blackUsername() == null) {
                    updatedGame = updatedGame.updateBlackUsername(username);
                }
                else if (targetGame.whiteUsername() == null) {
                    updatedGame = updatedGame.updateWhiteUsername(username);
                }
                game.put(gameID, updatedGame);
                return true;
            }
        }
        return false;
    }

    public boolean isTeamReqTaken(GameData targetGame, String requestedTeam) {
        if (Objects.equals(requestedTeam, "BLACK") && targetGame.blackUsername() != null) {
            return true;
        } else if (Objects.equals(requestedTeam, "WHITE") && targetGame.whiteUsername() != null) {
            return true;
        } else if (Objects.equals(requestedTeam, "WHITE/BLACK") && targetGame.whiteUsername() != null && targetGame.blackUsername() != null) {
            return true;
        } return false;
    }

    private String getUsername(String authToken) {
        return userAccess.getUsernameFromAuthToken(authToken);
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
