package client;

import client.apiclients.JoinGameRequest;
import exception.ResponseException;
import model.AuthData;
import model.UserData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<Integer, Integer> gameMap = new HashMap<>();

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
                    - help
                    """;
        }

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
        if (params.length == 2) {
            state = State.SIGNEDIN;
            username = params[0];
            password = params[1];

            UserData user = new UserData(username, null, password);
            AuthData authData = server.login(user);
            authToken = authData.authToken();
//            listGames();
            return String.format("You signed in as %s", username); // should I connect this to the api?
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String playGame(String[] params) throws ResponseException {
        if (params.length >= 2) {
            try {
                gameNumber = params[0];
                int intGameID = Integer.parseInt(gameNumber);
                if (intGameID > gameMap.size()) {
                    throw new ResponseException(400, "please choose a correct game number");
                }
                desiredTeam = params[1];
                desiredTeam = desiredTeam.toUpperCase();
                int actualGameID = gameMap.get(intGameID); // put in a try catch block to print nicely
                JoinGameRequest joinGameRequest = server.joinGame(desiredTeam, actualGameID);

                ui.ChessBoardUI.printChessBoard(desiredTeam);

                return String.format("You joined game %s as %s", gameNumber, desiredTeam);
            } catch (NumberFormatException e) {
                System.out.println("Invalid game number format: " + e.getMessage());
            } catch (NullPointerException e) {
                System.out.println("Game ID not found: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
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
//        int i = 1;
        // need another map
        List<Map<String, Object>> gamesList = server.listGames();

        StringBuilder formattedList = new StringBuilder("Game list:\n");

        for (int i = 0; i < gamesList.size(); i++) {
            Map<String, Object> game = gamesList.get(i);
            Integer gameId = ((Double) game.get("gameID")).intValue();
            String gameName = (String) game.get("gameName");
            String blackUsername = game.containsKey("blackUsername") ? (String) game.get("blackUsername") : " ";
            String whiteUsername = game.containsKey("whiteUsername") ? (String) game.get("whiteUsername") : " ";
            gameMap.put(i + 1, gameId);

            formattedList.append(String.format("%d. Game Name: %s\n   Black Username: %s\n   White Username: %s\n",
                    i + 1, gameName, blackUsername, whiteUsername));
        }
        return formattedList.toString();
    }

    public String quit() throws ResponseException {
        state = State.SIGNEDOUT;
        System.out.println("Goodbye!");
        System.exit(0);
        return "";
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
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
