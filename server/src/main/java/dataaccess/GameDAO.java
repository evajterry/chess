package dataaccess;

import handlers.exception.ResponseException;
import model.GameData;

import java.util.List;
import java.util.Map;

public interface GameDAO {
    void deleteAllData() throws ResponseException;
    String createNewGame(String authToken, String gameName) throws ResponseException;
    List<Map<String, Object>> listGames(String authToken) throws ResponseException;
    boolean joinNewGame(String authToken, int gameID, String requestedTeam) throws ResponseException, DataAccessException;
    boolean isTeamReqTaken(GameData targetGame, String requestedTeam);

}
