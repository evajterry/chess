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
    private DBConfig configuration;
    private SqlAuthAccess sqlAuthAccess;
    private GameAccess gameAccess;

    public SqlGameAccess(SqlAuthAccess sqlAuthAccess, GameAccess gameAccess) {
        this.configuration = new DBConfig();
        this.sqlAuthAccess = sqlAuthAccess;
        this.gameAccess = gameAccess;
    }

    public void deleteAllData() throws ResponseException {
        var statement = "TRUNCATE GameData";
        configuration.executeUpdate(statement);
    }

    public String createNewGame(String authToken, String gameName) throws ResponseException {
        // GameData(String whiteUsername, String blackUsername, int gameID, String gameName, ChessGame game)
        if (isValidLogIn(authToken)) {
            int gameID = generateNewID();
//            ChessGame newChessGame = new ChessGame();
            String jsonGameState = new Gson().toJson(new ChessGame()); // this is 아마 안 맞다
//            GameData newGame = new GameData(null, null, gameID, gameName, newChessGame);
            String statement = "INSERT INTO GameData (whiteUsername, blackUsername, gameID, gameName, game) VALUES (?, ?, ?, ?, ?)";
            configuration.executeUpdate(statement, null, null, gameID, gameName, jsonGameState);
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

        String query = "SELECT gameID, whiteUsername, blackUsername, gameName FROM GameData";
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
            throw new ResponseException(500, "error retrieving games ");
        }
        return gamesList;
    }

    public boolean joinNewGame2(String authToken, int gameID, String requestedTeam) throws ResponseException, DataAccessException {
        try {
            if (!sqlAuthAccess.userLoggedIn(authToken)) {
                throw new ResponseException(401, "Error: unauthorized");
            }
            String username = getUsername(authToken);
            if (!checkGameIDExists(gameID)) {
                throw new ResponseException(404, "Error: game not found");
            }
            GameData targetGame = getGameData(gameID);

            if (isTeamReqTaken(targetGame, requestedTeam)) {
                return false;
            }
            GameData updatedGame = targetGame;
            switch (requestedTeam) {
                case "white" -> {
                    if (targetGame.whiteUsername() == null) {
                        updatedGame = updatedGame.updateWhiteUsername(username);
                    } else {
                        throw new ResponseException(400, "Error: White team already taken");
                    }
                }
                case "black" -> {
                    if (targetGame.blackUsername() == null) {
                        updatedGame = updatedGame.updateBlackUsername(username);
                    } else {
                        throw new ResponseException(400, "Error: Black team already taken");
                    }
                }
                case "white/black" -> {
                    if (targetGame.whiteUsername() == null) {
                        updatedGame = updatedGame.updateWhiteUsername(username);
                    } else if (targetGame.blackUsername() == null) {
                        updatedGame = updatedGame.updateBlackUsername(username);
                    } else {
                        throw new ResponseException(400, "Error: both teams are already taken");
                    }
                }
                case null, default -> throw new ResponseException(400, "Error: invalid team request");
            }
            return updateGameInDatabase(updatedGame);
        } catch (ResponseException e) {
            // Log the exception message before re-throwing
            System.out.println("Caught ResponseException: " + e.getMessage());
            throw e;  // Re-throw to propagate the error
        }
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
                return false;
            }
            GameData updatedGame = targetGame;
            switch (requestedTeam) {
                case "WHITE" -> {
                    System.out.println("on line 86");
                    if (targetGame.whiteUsername() == null) {
                        updatedGame = updatedGame.updateWhiteUsername(username);
                        System.out.println("on line 89, game is updated to include white user");
                    } else {
                        System.out.println("about to throw an exception");
                        throw new ResponseException(400, "Error: White team already taken");
                    }
                }
                case "BLACK" -> {
                    if (targetGame.blackUsername() == null) {
                        updatedGame = updatedGame.updateBlackUsername(username);
                    } else {
                        throw new ResponseException(400, "Error: Black team already taken");
                    }
                }
                case "WHITE/BLACK" -> {
                    if (targetGame.whiteUsername() == null) {
                        updatedGame = updatedGame.updateWhiteUsername(username);
                    } else if (targetGame.blackUsername() == null) {
                        updatedGame = updatedGame.updateBlackUsername(username);
                    } else {
                        throw new ResponseException(400, "Error: both teams are already taken");
                    }
                }
                case null, default -> throw new ResponseException(400, "Error: invalid team request");
            }
        return updateGameInDatabase(updatedGame);
    }

    private boolean updateGameInDatabase(GameData updatedGame) {
        String updateQuery = "UPDATE GameData SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(updateQuery)) {
                ps.setString(1, updatedGame.whiteUsername());
                ps.setString(2, updatedGame.blackUsername());
                ps.setString(3, updatedGame.gameName());
                String serializedGame = serializeGame(updatedGame.game()); // here
                ps.setString(4, serializedGame);
                ps.setInt(5, updatedGame.gameID());

                int rowsAffected = ps.executeUpdate();

                return rowsAffected == 1;
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String serializeGame(ChessGame game) {
        return new Gson().toJson(game);
    }

    public GameData getGameData(int gameID) throws DataAccessException {
        String query = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM GameData WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(query)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    String whiteUsername = rs.getString("whiteUsername");
                    String blackUsername = rs.getString("blackUsername");
                    String gameName = rs.getString("gameName");
                    String gameDataString = rs.getString("game");
                    ChessGame game = deserializeGame(gameDataString); // need to make this type ChessGame
                    return new GameData(whiteUsername, blackUsername, gameID, gameName, game);
                    // GameData(String whiteUsername, String blackUsername, int gameID, String gameName, ChessGame game) {
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game data for gameID: " + gameID);
        }
    }

    private ChessGame deserializeGame(String gameDataString) throws DataAccessException {
        if (gameDataString == null || gameDataString.isEmpty()) {
            return new ChessGame();
        }
        try {
            return new Gson().fromJson(gameDataString, ChessGame.class);
        } catch (Exception e) {
            throw new DataAccessException("Failed to deserialize ChessGame");
        }
    }

    public boolean isTeamReqTaken(GameData targetGame, String requestedTeam) {
        return gameAccess.isTeamReqTaken(targetGame, requestedTeam);
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

    private boolean checkGameIDExists(int gameID) {
        String query = "SELECT COUNT(*) FROM GameData WHERE gameID = ?";
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
        return sqlAuthAccess.userLoggedIn(authToken);
    }

    private int generateNewID() throws ResponseException {
        String query = "SELECT MAX(gameID) FROM GameData";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int maxID = rs.getInt(1);
                return (rs.wasNull()) ? 1 : maxID + 1;
            }
        } catch (SQLException e) {
            throw new ResponseException(500, "Error generating new ID: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        throw new ResponseException(500, "Failed to generate new ID");
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS GameData (
                `id` int NOT NULL AUTO_INCREMENT,
                `whiteUsername` varchar(256),
                `blackUsername` varchar(256),
                `gameID` int NOT NULL,
                `gameName` varchar(256),
                `game` TEXT DEFAULT NULL,  -- Add the 'game' column here
                PRIMARY KEY (`id`),
                INDEX(type),
                INDEX(name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """
    };
}
