package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;

import java.util.ArrayList;
import java.util.HashMap;

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
