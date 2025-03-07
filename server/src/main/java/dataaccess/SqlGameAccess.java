package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import handlers.exception.ResponseException;
import model.GameData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SqlGameAccess implements GameDAO {
    private final ArrayList<Integer> idList = new ArrayList<>();
    private DBConfig configuration;
    private SqlUserAccess sqlUserAccess;

    public SqlGameAccess(SqlUserAccess sqlUserAccess) {
        this.sqlUserAccess = sqlUserAccess;
    }

    public void deleteAllData() throws ResponseException {
        var statement = "TRUNCATE GameData";
        configuration.executeUpdate(statement);
        idList.clear(); //idk if i still need this - maybe
    }

    public String createNewGame(String authToken, String gameName) throws ResponseException {
        // GameData(String whiteUsername, String blackUsername, int gameID, String gameName, ChessGame game)
        if (isValidLogIn(authToken)) {
            int gameID = generateNewID();

            GameData newGame = new GameData(null, null, gameID, gameName, new ChessGame());
            String jsonGameState = new Gson().toJson(newGame); // this is 아마 안 맞다
            var statement = "INSERT INTO GameData (whiteUsername, blackUsername, gameID, gameName, newGame) VALUES (?, ?, ?, ?, ?)";

            var json = new Gson().toJson(authToken); // this is ㅇㅏ마 wrong
            var id = configuration.executeUpdate(statement, null, null, gameID, jsonGameState, gameName);
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
//        for (GameData gameData : game.values()) {
//            String gameIDString = Integer.toString(gameData.gameID());
//            String whiteUser = gameData.whiteUsername(); // maybe need to do : ? here
//            String blackUser = gameData.blackUsername();
//            String gameName = gameData.gameName();
//
//            Map<String, Object> gameMap = new HashMap<>();
//            gameMap.put("gameID", gameIDString);
//            gameMap.put("whiteUsername", whiteUser);
//            gameMap.put("blackUsername", blackUser);
//            gameMap.put("gameName", gameName);
//            gamesList.add(gameMap);
//        }
        return gamesList;
    }

    public boolean joinNewGame(String authToken, int gameID, String requestedTeam) {
//        String username = getUsername(authToken);
//        if (isValidLogIn(authToken) && checkGameIDExists(gameID)) {
//            GameData targetGame = game.get(gameID);
//
//            if (!isTeamReqTaken(targetGame, requestedTeam)) {
//                GameData updatedGame = targetGame;
//                if (Objects.equals(requestedTeam, "WHITE/BLACK")) {
//                    if (targetGame.whiteUsername() == null) {
//                        updatedGame = updatedGame.updateWhiteUsername(username);
//                    } else if (targetGame.blackUsername() == null) {
//                        updatedGame = updatedGame.updateBlackUsername(username);
//                    }
//                }
//                else if (targetGame.blackUsername() == null && Objects.equals(requestedTeam, "BLACK")) {
//                    updatedGame = updatedGame.updateBlackUsername(username);
//                }
//                else if (targetGame.whiteUsername() == null && Objects.equals(requestedTeam, "WHITE")) {
//                    updatedGame = updatedGame.updateWhiteUsername(username);
//                }
//                else if (targetGame.blackUsername() == null) {
//                    updatedGame = updatedGame.updateBlackUsername(username);
//                }
//                else if (targetGame.whiteUsername() == null) {
//                    updatedGame = updatedGame.updateWhiteUsername(username);
//                }
//                game.put(gameID, updatedGame);
//                return true;
//            }
//        }
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

    private String getUsername(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, json FROM GameData WHERE id=?";
            try (var ps = conn.prepareStatement(statement)) {
//                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
//                        return readUsername(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
//
//    private String readUsername (ResultSet rs) throws SQLException {
//        var id = rs.getInt("id");
//        var json = rs.getString("json");
//        var pet = new Gson().fromJson(json, GameAccess.class);
//        return pet.setId(id);
//    }

    private boolean checkGameIDExists(int gameID) {
        return(idList.contains(gameID));
    }

    private boolean isValidLogIn(String authToken) {
        return true;
//        return userAccess.userLoggedIn(authToken);
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

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS GameData (
              `id` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameID` int NOT NULL,
              `gameName` varchar(256),
              `game` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(type),
              INDEX(name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };
}
