package client;

import client.APIClients.JoinGameRequest;
import com.sun.nio.sctp.NotificationHandler;
import handlers.exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChessClient {
    private String username = null;
    private String email = null;
    private String password = null;
    private String desiredTeam = null;
    private String gameNumber = null;
    private String authToken = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        // notification handler used to be here - is that part of websocket instead?
    }

    public String preLogin() {
        return """
                - help
                - quit
                - login (enter username and password separated by a space)
                - register (enter username, email and password separated by spaces)
                """;
    }

    public String evalPreLogin(String input) throws ResponseException { // maybe put in a try catch block
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        if (state == State.SIGNEDOUT) {
            return switch (cmd) {
                case "quit" -> quit();
                case "login" -> login(params); // good
                case "register" -> register(params); // good
                default -> help();
            };
        } else { // (state == State.SIGNEDIN)
            return switch (cmd) {
                case "logout" -> logout(); // good
                case "quit" -> quit(); // good
                case "create-game" -> createGame(params); // good
                case "list-games" -> listGames(); // good
                case "join-game" -> playGame(params); // almost
                case "observe-game" -> observeGame(params);
                default -> help();
            };
        }
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - login <yourname> <password>
                    - register <yourname> <email> <password>
                    - quit
                    """;
        }
        // Logout, create game, list games, play game, observe game
        return """
                - logout
                - create-game <gamename>
                - list-games
                - join-game <gameID> [WHITE|BLACK]
                - observe-game <gameNumber>
                - quit
                - help
                """;
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            state = State.SIGNEDIN;
            username = params[0];
            password = params[1];
//            server.login(new UserData("sample username", "email", "pass")); // I should be passing in UserData to login right?
            UserData user = new UserData(username, null, password);
            AuthData authData = server.login(user);
            authToken = authData.authToken();
            return String.format("You signed in as %s with password %s", username, password); // should I connect this to the api?
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String playGame(String[] params) throws ResponseException { // figure out issues
        if (params.length >= 2) {
            gameNumber = params[0];
            int intGameID = Integer.parseInt(gameNumber);
            desiredTeam = params[1];
            desiredTeam = desiredTeam.toUpperCase();
            JoinGameRequest joinGameRequest = server.joinGame(desiredTeam, intGameID);
            ui.ChessBoardUI.chessBoard();
            return String.format("You joined game %s as %s", joinGameRequest.getGameID(), joinGameRequest.getPlayerColor()); // should I connect this to the api?
        }
        throw new ResponseException(400, "Expected: <gameNumber> <WHITE|BLACK>");
    }

    public String observeGame(String[] params) throws ResponseException {
        if (params.length >= 1) {
            gameNumber = params[0];
            server.observeGame(gameNumber);
            return String.format("You joined game %s as an observer", gameNumber); // should I connect this to the api?
        }
        throw new ResponseException(400, "Expected: <gameNumber>");
    }

    public String listGames() throws ResponseException {
        // not implemented yet
        List<Map<String, Object>> gamesList = server.listGames();
        return "Game list: " + gamesList;
    }

    public String quit() throws ResponseException {
        state = State.SIGNEDOUT;
        System.out.println("Goodbye!");
        System.exit(0);
        return "";
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            state = State.SIGNEDIN;
            username = params[0];
            email = params[1];  // Second element is the email
            password = params[2];

            UserData userData = new UserData(username, email, password);
            AuthData authData = server.register(userData);
            authToken = authData.authToken();
            return String.format("You are now registered and logged in as %s.", username);
        }
        throw new ResponseException(400, "Expected: <username> <email> <password>");
                //public record UserData(String username, String email, String password) {
    }

    public String logout() throws ResponseException {
        state = State.SIGNEDOUT;
        server.logout(authToken);

        return String.format("you signed out");
    }

    public String createGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            String gameName = String.join(" ", params);
            server.createNewGame(gameName);
            return String.format("Game created under the name %s.", gameName);
        } else {
            throw new ResponseException(400, "Expected: <gameName>");
        }
    }
}
