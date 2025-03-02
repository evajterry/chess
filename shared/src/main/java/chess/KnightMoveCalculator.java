package chess;

import java.util.ArrayList;
import java.util.Collection;

import static chess.MoveHelper.isPositionValid;
import static chess.MoveHelper.knight_rook_move;

public class KnightMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> validMoves = new ArrayList<>();

        int[] rowOffsets = {1, 2, 2, 1, -1, -2, -2, -1};
        int[] colOffsets = {-2, -1, 1, 2, 2, 1, -1, -2};

        knight_rook_move(position, validMoves, board, rowOffsets, colOffsets);

        return validMoves;
    }
}
