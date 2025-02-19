package service;
import dataaccess.GameAccess;
import handlers.exception.*;

public class GameService {
    private final GameAccess gameAccess;

    public GameService(GameAccess gameAccess) {
        this.gameAccess = gameAccess;
    }

    public void deleteAllData() throws ResponseException {
        gameAccess.deleteAllData();
    }
}