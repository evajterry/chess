package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        // Add logic to calculate queen's moves
        Collection<ChessMove> validMoves = new ArrayList<>(); // does declaring a type in line 8 instantiate anything?

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        int originalRow = position.getRow() - 1;
        int originalCol = position.getColumn() - 1;

        // calculator right upward diagonal movement
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol + i;
            int newRow = originalRow + i;
            if (!continueToAddNewPiece(newRow, newCol, teamColor, position, validMoves, board)) {
                break;
            }
        }

        // calculator right downward diagonal movement
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol + i;
            int newRow = originalRow - i;
            if (!continueToAddNewPiece(newRow, newCol, teamColor, position, validMoves, board)) {
                break;
            }
        }

        // calculator left upward diagonal movement
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol - i;
            int newRow = originalRow + i;
            if (!continueToAddNewPiece(newRow, newCol, teamColor, position, validMoves, board)) {
                break;
            }
        }

        // calculator left downward diagonal movement
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol - i;
            int newRow = originalRow - i;
            if (!continueToAddNewPiece(newRow, newCol, teamColor, position, validMoves, board)) {
                break;
            }
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