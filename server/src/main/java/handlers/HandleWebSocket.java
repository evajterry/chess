package handlers;

import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import service.AuthService;
import service.GameService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class HandleWebSocket {
    private final GameService gameService;
    private final AuthService authService;

    private static final ConcurrentHashMap<Session, String> activeSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Integer> sessionGameMap = new ConcurrentHashMap<>();

    private static final Gson gson = new Gson();

    public HandleWebSocket(GameService gameService, AuthService authService) {
        this.gameService = gameService;
        this.authService = authService;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("WebSocket connected: " + session.getRemoteAddress());

        // You might want to keep track of sessions for later communication
        activeSessions.put(session, "pending");

        // Send a "CONNECT" message to confirm connection
        UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, null, null);
        session.getRemote().sendString(gson.toJson(connectCommand));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, ResponseException {
        System.out.println("Received WebSocket message: " + message);

        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        String playerName = getPlayerNameFromAuthToken(command.getAuthToken(), command.getGameID()); // Method to determine player's name

        switch (command.getCommandType()) {
            case CONNECT:
                activeSessions.put(session, command.getAuthToken());
                sessionGameMap.put(session, command.getGameID());

                String notificationMessage = String.format("User %s joined game %d as a player.", playerName, command.getGameID());
                ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationMessage);
                broadcastToGame(command.getGameID(), notification, session);
                break;

            case OBSERVE:
                activeSessions.put(session, command.getAuthToken());
                sessionGameMap.put(session, command.getGameID());

                String obsNotificationMessage = String.format("User %s is observing game %d.", playerName, command.getGameID());
                ServerMessage obsNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, obsNotificationMessage);
                broadcastToGame(command.getGameID(), obsNotification, session);
                break;
            case LEAVE:
                String leavingMessage = String.format("User %s has left the game '%d.'", playerName, command.getGameID());
                ServerMessage leavingNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, leavingMessage);
                broadcastToGame(command.getGameID(), leavingNotification, session);
                activeSessions.remove(session);
                sessionGameMap.remove(session);
                session.close();
                break;

            case RESIGN:
                String resignMessage = String.format("User %s has resigned from the game %d. The game is now over.", playerName, command.getGameID());
                ServerMessage resignationNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, resignMessage);
                broadcastToGame(command.getGameID(), resignationNotification, session);
                break;
        }
    }

    private String getPlayerNameFromAuthToken(String authToken, int gameID) throws ResponseException {
        List<Map<String, Object>> gameData = gameService.listGames(authToken);

        for (Map<String, Object> gameMap : gameData) {
            if ((int) gameMap.get("gameID") == gameID) {
                String whiteUsername = (String) gameMap.get("whiteUsername");
                String blackUsername = (String) gameMap.get("blackUsername");

                String username = authTokenToUsername(authToken, whiteUsername, blackUsername);

                // Determine and return player and team information
                if (username.equals(whiteUsername)) {
                    return String.format("%s, playing as WHITE", username);
                } else if (username.equals(blackUsername)) {
                    return String.format("%s playing as BLACK", username);
                } else {
                    return "User not found in this game.";
                }
            }
        }
        return "didn't return anything else";
    }

    private String authTokenToUsername(String authToken, String whiteUsername, String blackUsername) {
        return authService.getUsernameFromAuthToken(authToken);
    }

    private String determinePlayerSide(String authToken) {
        // Logic to determine the player side (e.g., check a map or a service)
        return "black"; // or "white", according to your application logic
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
        activeSessions.remove(session);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error: " + error.getMessage());
    }

    /**
     * Broadcasts a message to all players in a specific game.
     */
    public static void broadcastToGame(int gameID, ServerMessage message, Session initiatingSession) {
        String jsonMessage = gson.toJson(message);

        for (var entry : sessionGameMap.entrySet()) {
            Session session = entry.getKey();
            int sessionGameID = entry.getValue();

            if (sessionGameID == gameID && !session.equals(initiatingSession)) {
                try {
                    session.getRemote().sendString(jsonMessage);
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                }
            }
        }
    }
}
