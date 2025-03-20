package client;

import com.sun.nio.sctp.NotificationHandler;
import handlers.exception.ResponseException;
import model.UserData;

import java.util.Arrays;

public class ChessClient {
    private String visitorName = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;
    // I deleted notification handler out of here


    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        // notification handler used to be here - is that part of websocket instead?
    }

    public String preLogin() {
        return """
                - help
                - quit
                - login
                - register
                """;
    }

    public String evalPreLogin(String input) throws ResponseException { // maybe put in a try catch block
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
//                case "help" -> help();
//                case "quit" -> quit();
                case "login" -> login(params);
                case "register" -> register(params);
            default -> help();
        };
    }

//    private String register() throws ResponseException {
//        if (params.length >= 1) {
//            state = State.SIGNEDIN;
//            visitorName = String.join("-", params);
//            return String.format("You signed in as %s.", visitorName); // should I connect this to the api?
//        }
//        throw new ResponseException(400, "Expected: <yourname>");
//
//    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - signIn <yourname>
                    - quit
                    """;
        }
        // Logout, create game, list games, play game, observe game
        return """
                - log out
                - create game
                - list games
                - play game
                - observe game
                - quit
                """;
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 1) {
            state = State.SIGNEDIN;
            visitorName = String.join("-", params);
            // how should I get type UserData from visitor name?
            server.login(new UserData("sample username", "email", "pass")); // I should be passing in UserData to login right?
            return String.format("You signed in as %s.", visitorName); // should I connect this to the api?
        }
        throw new ResponseException(400, "Expected: <yourname>");
    }

    public String register(String... params) throws ResponseException {
        state = State.SIGNEDIN;
        server.register(new UserData("sampleUser", "sampleEmail", "samplePath"));
        return String.format("You an account under the username %s.", visitorName);
    }

}
