import chess.*;
import client.Repl;
import exception.ResponseException;

public class Main {
    public static void main(String[] args) throws ResponseException {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        // error, circular dependencies
        System.out.println("♕ 240 Chess Client: " + piece);

        var serverUrl = "http://localhost:8081";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        new Repl(serverUrl).run();
    }
}