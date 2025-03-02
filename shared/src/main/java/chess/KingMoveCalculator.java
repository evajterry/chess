package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.MoveHelper.knightRookMove;

public class KingMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};

        knightRookMove(position, validMoves, board, rowOffsets, colOffsets);

        return validMoves;
    }
}