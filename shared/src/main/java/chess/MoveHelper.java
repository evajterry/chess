package chess;

import java.util.Collection;

public class MoveHelper {
    static boolean continueToAddNewPiece(int r, int c, ChessGame.TeamColor tC, ChessPosition po, Collection<ChessMove> vm, ChessBoard b) {
        if (r >= 0 && r < 8 && c >= 0 && c < 8) {
            ChessPosition newPosition = new ChessPosition(r + 1, c + 1);
            ChessPiece targetPiece = b.getPiece(newPosition);
            if (targetPiece == null) {
                vm.add(new ChessMove(po, newPosition, null));
                return true;
            }
            if (targetPiece.getTeamColor() != tC) {
                vm.add(new ChessMove(po, newPosition, null));
                return false;
            }
        }
        return false;
    }
    static boolean isPositionValid(int row, int col) {
        return row > 0 && row <= 8 && col > 0 && col <= 8;
    }

    static void knightRookMove(ChessPosition position, Collection<ChessMove> validMoves, ChessBoard board, int[] rowOffsets, int[] colOffsets) {
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
