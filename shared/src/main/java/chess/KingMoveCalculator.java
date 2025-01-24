package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};

        ChessPiece piece = board.getPiece(position);
        ChessGame.TeamColor teamColor = piece.getTeamColor();

        for (int i = 0; i < rowOffsets.length; i++) {

            int newRow = position.getRow() + rowOffsets[i];
            int newCol = position.getColumn() + colOffsets[i];

            if (isPositionValid(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece == null || targetPiece.getTeamColor() != teamColor) { // the board for test 1
                    validMoves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return validMoves;
    }

    private boolean isPositionValid(int row, int col) {
        return row > 0 && row <= 8 && col > 0 && col <= 8;
    }
}
