package client;

import com.sun.nio.sctp.NotificationHandler;
import handlers.exception.ResponseException;

import java.util.Arrays;

public class ChessClient {
    private String visitorName = null;
    private final ServerFacade server;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private State state = State.SIGNEDOUT;


    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
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
//                case "register" -> register();
            default -> help();
        };
    }

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
            return String.format("You signed in as %s.", visitorName);
        }
        throw new ResponseException(400, "Expected: <yourname>");
    }

}
