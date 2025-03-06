import chess.*;
import dataaccess.*;
import handlers.exception.ResponseException;
import server.Server;
import service.AuthService;
import service.GameService;
import service.UserService;

public class Main {
    public static void main(String[] args) throws ResponseException, DataAccessException {

        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        Server server = new Server();

        server.run(8080);
    }
}
