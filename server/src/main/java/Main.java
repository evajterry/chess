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

        Server server = new Server();
//        var service = new AuthService();
        server.run(8080);
    }
}
