import chess.*;
import dataaccess.AuthAccess;
import dataaccess.UserAccess;
import dataaccess.GameAccess;
import server.Server;
import service.AuthService;
import service.GameService;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
        var authAccess = new AuthAccess();  // Assuming AuthAccess has a default constructor
        var gameAccess = new GameAccess();
        var userAccess = new UserAccess();
        var authService = new AuthService(authAccess);
        var gameService = new GameService(gameAccess);
        var userService = new UserService(userAccess);

        Server server = new Server(authService, gameService, userService);
//        var service = new AuthService();
        server.run(8080);
    }
}
