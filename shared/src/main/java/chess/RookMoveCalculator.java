package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        int originalRow = position.getRow() - 1;
        int originalCol = position.getColumn() - 1;

        boolean up = true, down = true, left = true, right = true;

        for (int i = 1; i < 8; i++) {
            int newVertUpRow = originalRow + i;
            int newVertDownRow = originalRow - i;
            int newHorzRightCol = originalCol + i;
            int newHorzLeftCol = originalCol - i;
            if (up) {
                up = MoveHelper.continueToAddNewPiece(newVertUpRow, originalCol, teamColor, position, validMoves, board);
            }
            if (down) {
                down = MoveHelper.continueToAddNewPiece(newVertDownRow, originalCol, teamColor, position, validMoves, board);
            }
            if (right) {
                right = MoveHelper.continueToAddNewPiece(originalRow, newHorzRightCol, teamColor, position, validMoves, board);
            }
            if (left) {
                left = MoveHelper.continueToAddNewPiece(originalRow, newHorzLeftCol, teamColor, position, validMoves, board);
            }
            ;
        }
        return validMoves;
    }
}