package client;

import com.sun.nio.sctp.NotificationHandler;
import handlers.exception.ResponseException;
import model.UserData;
import com.google.gson.Gson;

import java.util.Arrays;

public class ChessClient {
    private String username = null;
    private String email = null;
    private String password = null;
    private String desiredTeam = null;
    private String gameNumber = null;
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
                case "login" -> login(params);
                case "register" -> register(params);
                default -> help();
            };
        } else { // (state == State.SIGNEDIN)
            return switch (cmd) {
                case "logout" -> logout();
                case "quit" -> quit();
                case "create-game" -> createGame(params);
                case "list-games" -> listGames();
                case "play-game" -> playGame(params);
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
                - play-game <gamename> [WHITE|BLACK]
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
            server.login(user);
            return String.format("You signed in as %s with password %s", username, password); // should I connect this to the api?
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String playGame(String[] params) throws ResponseException {
        if (params.length >= 2) {
            gameNumber = params[0];
            desiredTeam = params[1];
            return String.format("You joined game %s as %s", gameNumber, desiredTeam); // should I connect this to the api?
        }
        throw new ResponseException(400, "Expected: <gameNumber> <desiredTeam>");
    }

    public String observeGame(String[] params) throws ResponseException {
        if (params.length >= 1) {
            gameNumber = params[0];
            return String.format("You joined game %s as an observer", gameNumber); // should I connect this to the api?
        }
        throw new ResponseException(400, "Expected: <gameNumber>");
    }

    public String listGames() {
        // call to the api
        // api returns list of games
//        return String.format("Games: \n ", )
        return "Game list: ";
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
            server.register(userData);
            return String.format("You an account under the username %s.", username);
        }
        throw new ResponseException(400, "Expected: <username> <email> <password>");
                //public record UserData(String username, String email, String password) {
    }

    public String logout() throws ResponseException {
        state = State.SIGNEDOUT;
        server.logout();
        return String.format("you signed out");
    }

    public String createGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            String gameName = String.join(" ", params);
            return String.format("Game created under the name %s.", gameName);
        } else {
            throw new ResponseException(400, "Expected: <gameName>");
        }
    }
}
