package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        int[] rowOffsets = {1, 2, 2, 1, -1, -2, -2, -1};
        int[] colOffsets = {-2, -1, 1, 2, 2, 1, -1, -2};

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor teamColor = piece.getTeamColor();

        for (int i = 0; i < rowOffsets.length; i++) {

            int newRow = position.getRow() + rowOffsets[i];
            int newCol = position.getColumn() + colOffsets[i];

            if (isPositionValid(newRow - 1, newCol - 1)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null || targetPiece.getTeamColor() != teamColor) {
                    validMoves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return validMoves;
    }

    private boolean isPositionValid(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
