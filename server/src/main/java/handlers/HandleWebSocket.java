package handlers;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class HandleWebSocket {
    private final GameService gameService;

    private static final ConcurrentHashMap<Session, String> activeSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, Integer> sessionGameMap = new ConcurrentHashMap<>();

    private static final Gson gson = new Gson();

    public HandleWebSocket(GameService gameService) {
        this.gameService = gameService;

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
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("Received WebSocket message: " + message);

        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT:
                activeSessions.put(session, command.getAuthToken());
                System.out.println("User connected with authToken: " + command.getAuthToken());

                String notificationMessage = String.format("User %s connected to the game", command.getAuthToken());
                ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationMessage);

                broadcastToGame(Integer.valueOf(command.getGameID()), notification); // Ensure gameID conversion to Integer
                break;
            case MAKE_MOVE:
                // Handle game moves
                break;
            case LEAVE:
                session.close();
                break;
            case RESIGN:
                // Handle resignation
                break;
        }
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
    public static void broadcastToGame(int gameID, ServerMessage message) {
        String jsonMessage = gson.toJson(message);

        for (var entry : sessionGameMap.entrySet()) {
            Session session = entry.getKey();
            int sessionGameID = entry.getValue();

            if (sessionGameID == gameID) {
                try {
                    session.getRemote().sendString(jsonMessage);
                } catch (IOException e) {
                    System.err.println("Error sending message: " + e.getMessage());
                }
            }
        }
    }

}

//    public Object handle(Request req, Response res) throws ResponseException, DataAccessException {
////        try {
//            String authToken = req.headers("Authorization");
//            var gameData = new Gson().fromJson(req.body(), GameData.class);
//            int gameID = gameData.gameID();
//            String reqTeam = gameData.blackUsername();
//            if (reqTeam == null) {
//                reqTeam = gameData.whiteUsername();
//            }
//            gameService.joinNewGame(authToken, gameID, reqTeam);
////            List<Map<String, Object>> games = gameService.listGames(authToken);
//            res.status(200);
//
////            return Serializer.listOfGames(games);
//
////        } catch (ResponseException e) {
////            res.status(e.statusCode());
////            return gson.toJson(new ErrorResponse(e.getMessage()));
//        return "websocket handler? ";
//    }
//}
