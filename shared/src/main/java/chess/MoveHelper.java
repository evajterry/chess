package chess;

import java.util.Collection;

public class MoveHelper {
    static boolean continueToAddNewPiece(int newRow, int col, ChessGame.TeamColor teamColor, ChessPosition position, Collection<ChessMove> validMoves, ChessBoard board) {
        if (newRow >= 0 && newRow < 8 && col >= 0 && col < 8) {
            ChessPosition newPosition = new ChessPosition(newRow + 1, col + 1);
            ChessPiece targetPiece = board.getPiece(newPosition);
            if (targetPiece == null) {
                validMoves.add(new ChessMove(position, newPosition, null));
                return true;
            }
            if (targetPiece.getTeamColor() != teamColor) {
                validMoves.add(new ChessMove(position, newPosition, null));
                return false;
            }
        }
        return false;
    }
    static boolean isPositionValid(int row, int col) {
        return row > 0 && row <= 8 && col > 0 && col <= 8;
    }

    static void knight_rook_move(ChessPosition position, Collection<ChessMove> validMoves, ChessBoard board, int[] rowOffsets, int[] colOffsets) {
        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor teamColor = piece.getTeamColor();

        for (int i = 0; i < rowOffsets.length; i++) {

            int newRow = position.getRow() + rowOffsets[i];
            int newCol = position.getColumn() + colOffsets[i];

            if (isPositionValid(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null || targetPiece.getTeamColor() != teamColor) {
                    validMoves.add(new ChessMove(position, newPosition, null));
                }
            }
        }
    }
}
