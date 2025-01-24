package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>(); // does declaring a type in line 8 instantiate anything?

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        int originalRow = position.getRow() - 1;
        int originalCol = position.getColumn() - 1;
        // calculate the vertical upward movements

        for (int i = 1; i < 8; i++) {
            int newRow = originalRow + i;
//            int col = position.getColumn();
            if (!continueToAddNewPiece(newRow, originalCol, teamColor, position, validMoves, board)) {
                break;
            };
        }
        // calculate the vertical downward movements
        for (int i = 1; i < 8; i++) {
            int newRow = originalRow - i; // this is where it's different from last one - moving it down
//            int col = position.getColumn();
            if (!continueToAddNewPiece(newRow, originalCol, teamColor, position, validMoves, board)) {
                break;
            };
        }

        // calculate the horizontal rightward movements
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol + i; // this is where it's different from last one - moving it down
//            int row = position.getRow();
            if (!continueToAddNewPiece(originalRow, newCol, teamColor, position, validMoves, board)) {
                break;
            };
        }

        // calculate the horizontal leftward movements
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol - i; // this is where it's different from last one - moving it down
//            int row = position.getRow();
            if (!continueToAddNewPiece(originalRow, newCol, teamColor, position, validMoves, board)) {
                break;
            };
        }

        return validMoves; // Placeholder
    }
    boolean continueToAddNewPiece(int newRow, int col, ChessGame.TeamColor teamColor, ChessPosition position, Collection<ChessMove> validMoves, ChessBoard board) {
        if (newRow >= 0 && newRow < 8 && col >= 0 && col < 8) {
            ChessPosition newPosition = new ChessPosition(newRow + 1, col + 1);
            ChessPiece targetPiece = board.getPiece(newPosition); // when getting the piece, it subtracts another from each
            // index, giving an out of range error
            if (targetPiece == null) {
                validMoves.add(new ChessMove(position, newPosition, null));
                return true;
            }
            if (targetPiece.getTeamColor() != teamColor) {
                validMoves.add(new ChessMove(position, newPosition, null));
                return false;
            }
        }
        return false; // return false if the target position is filled by your own color
    }
}