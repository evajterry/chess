package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        // Add logic to calculate queen's moves
        Collection<ChessMove> validMoves = new ArrayList<>();

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        int originalRow = position.getRow() - 1;
        int originalCol = position.getColumn() - 1;

        // calculator right upward diagonal movement
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol + i;
            int newRow = originalRow + i;
            if (!MoveHelper.continueToAddNewPiece(newRow, newCol, teamColor, position, validMoves, board)) {
                break;
            }
        }

        // calculator right downward diagonal movement
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol + i;
            int newRow = originalRow - i;
            if (!MoveHelper.continueToAddNewPiece(newRow, newCol, teamColor, position, validMoves, board)) {
                break;
            }
        }

        // calculator left upward diagonal movement
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol - i;
            int newRow = originalRow + i;
            if (!MoveHelper.continueToAddNewPiece(newRow, newCol, teamColor, position, validMoves, board)) {
                break;
            }
        }

        // calculator left downward diagonal movement
        for (int i = 1; i < 8; i++) {
            int newCol = originalCol - i;
            int newRow = originalRow - i;
            if (!MoveHelper.continueToAddNewPiece(newRow, newCol, teamColor, position, validMoves, board)) {
                break;
            }
        }

        return validMoves; // Placeholder
    }
}