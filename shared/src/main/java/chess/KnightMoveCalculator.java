package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.MoveHelper.knightRookMove;

public class KnightMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        int[] rowOffsets = {1, 2, 2, 1, -1, -2, -2, -1};
        int[] colOffsets = {-2, -1, 1, 2, 2, 1, -1, -2};

        knightRookMove(position, validMoves, board, rowOffsets, colOffsets);

        return validMoves;
    }
}
