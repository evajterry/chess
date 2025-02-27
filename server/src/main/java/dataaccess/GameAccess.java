package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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
            System.out.print(newGame);
            return String.valueOf(gameID);
        } else {
            return "Error: unauthorized";
        }
    }

    public void joinNewGame(String authToken, String username, int gameID, String requestedTeam) {
        if (isValidLogIn(authToken) && checkGameIDExists(gameID)) {
            GameData targetGame = game.get(gameID);
            // should I check if the game is full? and should this function be void?
            //if (targetGame.blackUsername() == null )
            if (targetGame.blackUsername() == null && Objects.equals(requestedTeam, "BLACK")) {
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
