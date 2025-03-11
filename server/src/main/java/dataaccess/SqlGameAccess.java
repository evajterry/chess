package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import handlers.exception.ResponseException;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SqlGameAccess implements GameDAO {
    private final ArrayList<Integer> idList = new ArrayList<>();
    private DBConfig configuration;
    private SqlUserAccess sqlUserAccess;
    private SqlAuthAccess sqlAuthAccess;

    public SqlGameAccess(SqlUserAccess sqlUserAccess, SqlAuthAccess sqlAuthAccess) {
        this.sqlUserAccess = sqlUserAccess;
        this.sqlAuthAccess = sqlAuthAccess;
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
        if (!sqlAuthAccess.userLoggedIn(authToken)) {
            throw new ResponseException(401, "Error: unauthorized");
        }
        List<Map<String, Object>> gamesList = new ArrayList<>();

        String query = "SELECT gameID, whiteUsername, blackUsername, gameName FROM Games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> gameMap = new HashMap<>();
                gameMap.put("gameID", rs.getInt("gameID"));
                gameMap.put("whiteUsername", rs.getString("whiteUsername"));
                gameMap.put("blackUsername", rs.getString("blackUsername"));
                gameMap.put("gameName", rs.getString("gameName"));

                gamesList.add(gameMap);
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        return gamesList;
    }

        public boolean joinNewGame(String authToken, int gameID, String requestedTeam) throws ResponseException, DataAccessException {
            if (!sqlAuthAccess.userLoggedIn(authToken)) {
                throw new ResponseException(401, "Error: unauthorized");
            }
            String username = getUsername(authToken);
            if (!checkGameIDExists(gameID)) {
                throw new ResponseException(404, "Error: game not found");
            }
            GameData targetGame = getGameData(gameID);

            if (isTeamReqTaken(targetGame, requestedTeam)) {
                return false; // Team is already taken
            }
            GameData updatedGame = targetGame;
            if (Objects.equals(requestedTeam, "WHITE")) {
                if (targetGame.whiteUsername() == null) {
                    updatedGame = updatedGame.updateWhiteUsername(username);
                } else {
                    throw new ResponseException(400, "Error: White team already taken");
                }
            } else if (Objects.equals(requestedTeam, "BLACK")) {
                if (targetGame.blackUsername() == null) {
                    updatedGame = updatedGame.updateBlackUsername(username);
                } else {
                    throw new ResponseException(400, "Error: Black team already taken");
                }
            } else if (Objects.equals(requestedTeam, "WHITE/BLACK")) {
                if (targetGame.whiteUsername() == null) {
                    updatedGame = updatedGame.updateWhiteUsername(username);
                } else if (targetGame.blackUsername() == null) {
                    updatedGame = updatedGame.updateBlackUsername(username);
                } else {
                    throw new ResponseException(400, "Error: both teams are already taken");
                }
            } else {
                throw new ResponseException(400, "Error: invalid team request");
            } // need to update the game in the database
//                game.put(gameID, updatedGame);
//                return true;
//            }
//        }
        return false;
    }

    private GameData getGameData(int gameID) throws DataAccessException {
        String query = "SELECT gameID, whiteUsername, blackUsername, gameName FROM Games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(query)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                // GameData(String whiteUsername, String blackUsername, int gameID, String gameName, ChessGame game) {
                if (rs.next()) {
                    int id = rs.getInt("gameID");
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String gameDataString = rs.getString("game");
                    ChessGame game = deserializeGame(gameDataString);
                    return new GameData(whiteUsername, blackUsername, gameID, gameName, game);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game data for gameID: " + gameID);
        }
    }

    private ChessGame deserializeGame(String gameDataString) throws DataAccessException {
        try {
            return new Gson().fromJson(gameDataString, ChessGame.class);
        } catch (Exception e) {
            throw new DataAccessException("Error deserializing game data");
        }
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
            String query = "SELECT username FROM AuthData WHERE authToken = ?";
            try (var ps = conn.prepareStatement(query)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error accessing database while retrieving username");
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
        String query = "SELECT COUNT(*) FROM Games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(query)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean isValidLogIn(String authToken) {
        sqlAuthAccess.userLoggedIn(authToken);
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
            CREATE TABLE IF NOT EXISTS Games (
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
