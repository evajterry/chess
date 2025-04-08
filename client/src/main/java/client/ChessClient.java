package client;

import chess.*;
import client.apiclients.JoinGameRequest;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import ui.ChessBoardUI;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.*;
import java.util.stream.Collectors;

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
    private ChessGame chessGame;

    private WebSocketFacade ws;
    private final NotificationHandler notificationHandler;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) throws ResponseException {
        server = new ServerFacade(serverUrl);
        this.ws = new WebSocketFacade(serverUrl, notificationHandler); // move
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        this.chessGame = new ChessGame();
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

    public String evalGamePlay(String input) throws ResponseException {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "redraw-board" -> redrawBoard();
            case "resign" -> resign();
            case "leave-game" -> leaveGame();
            case "highlight-moves" -> highlightMoves();
            case "make-move" -> makeMoves();
            default -> gameHelp();
        };
    }

    private String makeMoves() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the position of the piece you want to move (example: 'e2'):");
        String firstPos = scanner.nextLine().trim();
        ChessPosition startPosition = convertInputToChessPosition(firstPos);

        System.out.println("Enter the position of where you want the piece to go (example: 'e4'):");
        String secondPos = scanner.nextLine().trim();
        ChessPosition endPosition = convertInputToChessPosition(secondPos);

        if (startValid(startPosition)) {
            Collection<ChessMove> validMoves = chessGame.validMoves(startPosition);
            System.out.println("ValidMoves" + validMoves);

            String validMovesNotation = validMoves.stream()
                    .map(move -> convertPositionToChessNotation(move.getEndPosition()))
                    .collect(Collectors.joining(", "));
            System.out.println("validMovesNotation: " + validMovesNotation);

            Collection<ChessPosition> endPositions = validMoves.stream()
                    .map(ChessMove::getEndPosition)
                    .collect(Collectors.toList());

            System.out.println("endPositions: " + endPositions);

            if (endPositions.contains(endPosition)) {
                System.out.println("endPositions contains endPosition! ");
                updateChessGame(startPosition, endPosition);
            }
        }
        return startPosition + " " + endPosition;
    }

    private void updateChessGame(ChessPosition startPosition, ChessPosition endPosition) {
        ChessMove move = new ChessMove(startPosition, endPosition, null);
        try {
            chessGame.makeMove(move);
        } catch (InvalidMoveException e) {
            System.out.println("Invalid move: " + e.getMessage());
            return;
        }
        ChessPiece piece = chessGame.getBoard().getPiece(endPosition);
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            boolean needsPromotion = (piece.getTeamColor() == ChessGame.TeamColor.BLACK && endPosition.getRow() == 1) ||
                    (piece.getTeamColor() == ChessGame.TeamColor.WHITE && endPosition.getRow() == 8);
            if (needsPromotion) {
                promptForPromotion(endPosition, piece.getTeamColor());
            }
        }
        ChessBoardUI.printUpdatedBoard(chessGame.getBoard(), desiredTeam);
    }

    private void promptForPromotion(ChessPosition endPosition, ChessGame.TeamColor teamColor) {
        Scanner scanner = new Scanner(System.in);
        ChessPiece.PieceType promotionType = null;
        while (promotionType == null) {
            System.out.println("Enter the promotion piece you want (options: QUEEN, ROOK, BISHOP, KNIGHT): ");
            String promPiece = scanner.nextLine().trim().toUpperCase();
            try {
                promotionType = ChessPiece.PieceType.valueOf(promPiece);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid promotion type; please try again.");
            }
        }
        chessGame.getBoard().addPiece(endPosition, new ChessPiece(teamColor, promotionType));
        System.out.println("Pawn promoted to " + promotionType);
    }

    private boolean endValid(ChessPosition endPosition) {
        return true;
    }

    private boolean startValid(ChessPosition startPosition) {
        if (startPosition == null || chessGame.getBoard().getPiece(startPosition) == null) {
            System.out.println("You either entered an invalid position or there is no piece here.");
            return false;
        }
        ChessPiece piece = chessGame.getBoard().getPiece(startPosition);
        if (piece == null) {
            System.out.println("No piece at this position.");
            return false;
        }
        ChessGame.TeamColor targetColor = piece.getTeamColor();
        ChessGame.TeamColor playerTeamColor;
        try {
            playerTeamColor = ChessGame.TeamColor.valueOf(desiredTeam.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid team designation.");
            return false;
        }
        if (targetColor != playerTeamColor) {
            System.out.println("The piece you selected isn't your team >:( ");
        }
        return true;
    }

    private String highlightMoves() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the position you want to check (example: 'e2'):");
        String input = scanner.nextLine().trim();
        ChessPosition startPosition = convertInputToChessPosition(input);

        if (startValid(startPosition)) {
            Collection<ChessMove> validMoves = chessGame.validMoves(startPosition);
            if (validMoves == null || validMoves.isEmpty()) {
                return "No valid moves for this piece.";
            }
            String validMovesNotation = validMoves.stream()
                    .map(move -> convertPositionToChessNotation(move.getEndPosition()))
                    .collect(Collectors.joining(", "));
            Collection<ChessPosition> endPositions = validMoves.stream()
                    .map(ChessMove::getEndPosition)  // map each move to its end position
                    .collect(Collectors.toList());
            ChessBoard board = chessGame.getBoard();
            ui.ChessBoardUI.printChessBoard(desiredTeam, board, endPositions);

            return "Valid moves: " + validMovesNotation;
        }
        return "Please enter correct positions.";
    }

    private String convertPositionToChessNotation(ChessPosition position) {
        char file = (char) ('a' + position.getColumn() - 1); // convert column to letter
        int rank = position.getRow();
        return "" + file + rank;
    }

    private ChessPosition convertInputToChessPosition(String input) {
        if (input.length() != 2) return null;
        char letter = input.charAt(0);
        char num = input.charAt(1);

        if (letter < 'a' || letter > 'h' || num < '1' || num > '8') return null;
        int row = Character.getNumericValue(num);
        int col = letter - 'a' + 1;

        return new ChessPosition(row, col);
    }

    private String leaveGame() {
        try {
            this.ws.enterGame(new UserGameCommand(
                    UserGameCommand.CommandType.LEAVE,
                    authToken,
                    Integer.parseInt(gameNumber)));

            this.ws.closeConnection();

            gameNumber = null;
            desiredTeam = null;
            System.out.println("You have left the game. Returning to main menu.");

            return "Bye bye!\n main menu:\n";
        } catch (Exception e) {
            System.out.println("An error occurred while leaving the game: " + e.getMessage());
            return "Failed to leave the game.";
        }
    }

    private String resign() throws ResponseException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Are you sure you want to resign? <yes>|<no>");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            this.ws.enterGame(new UserGameCommand(
                    UserGameCommand.CommandType.RESIGN,
                    authToken,
                    Integer.parseInt(gameNumber)));

            System.out.println("You have resigned from the game. The game is now over.");
            return "you resigned!";
        }
        return "continue playing!";
    }

    private String gameHelp() {
        return """
                    - redraw-board
                    - leave-game
                    - make-move
                    - resign
                    - highlight-moves
                    """;
    }

    private String redrawBoard() {
        ChessBoard board = chessGame.getBoard();
        ui.ChessBoardUI.printChessBoard(desiredTeam, board, null);
        return " ";
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
            listGames();
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
                int actualGameID = gameMap.get(intGameID);

                JoinGameRequest joinGameRequest = server.joinGame(desiredTeam, actualGameID);
                this.ws.enterGame(new UserGameCommand(
                        UserGameCommand.CommandType.CONNECT,
                        authToken,
                        intGameID));
                System.out.println(String.format("You joined game %s as %s", gameNumber, desiredTeam));
                ChessBoard board = chessGame.getBoard();
                ui.ChessBoardUI.printChessBoard(desiredTeam, board, null);
                return replLoop();
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

    private String replLoop() throws ResponseException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Type 'help' to see what you can do!");
        while (true) {
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("leave-game")) {
                return leaveGame();
            } else {
                String commandResult = evalGamePlay(input);
                if (!commandResult.isBlank()) {
                    System.out.println(commandResult);
                }
            }
        }
    }

    private void broadcastGameJoin(int actualGameID, String desiredTeam) {
        String notificationMessage = String.format("A player joined the game %d as %s", actualGameID, desiredTeam);
        ServerMessage joinNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, notificationMessage);

//        HandleWebSocket.broadcastToGame(actualGameID, joinNotification);
    }

    public String observeGame(String[] params) throws ResponseException {
        if (params.length >= 1) {
            try {
                gameNumber = params[0];
                int intGameID = Integer.parseInt(gameNumber);

                if (!gameMap.containsValue(intGameID)) {
                    throw new ResponseException(400, "please choose a correct game number");
                }

                this.ws.enterGame(new UserGameCommand(
                        UserGameCommand.CommandType.OBSERVE,
                        authToken,
                        intGameID));
                ChessBoard board = chessGame.getBoard();
                ui.ChessBoardUI.printChessBoard("WHITE", board, null);

                return String.format("You joined game %s as an observer", gameNumber);
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
        throw new ResponseException(400, "Expected: <gameNumber>");
    }

// WHAT OBSERVE SHOULD DO

//    Call the server join HTTP API to join them to the game. This step is only done for players. Observers do not need to make the join HTTP API request.
//    Open a WebSocket connection with the server (using the /ws endpoint) so it can send and receive gameplay messages.
//    Send a CONNECT WebSocket message to the server.
//    Transition to the gameplay UI. The gameplay UI draws the chess board and allows the user to perform the gameplay commands described in the previous section.

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
