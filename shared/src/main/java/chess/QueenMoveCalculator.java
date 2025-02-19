package chess;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMoveCalculator implements ChessPieceMovesCalculator {
    private final BishopMoveCalculator bishopMoveCalculator = new BishopMoveCalculator();
    private final RookMoveCalculator rookMoveCalculator = new RookMoveCalculator();
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        // Add logic to calculate queen's moves
        Collection<ChessMove> validMoves = new ArrayList<>(); // does declaring a type in line 8 instantiate anything?

        validMoves.addAll(bishopMoveCalculator.pieceMoves(board, position));
        validMoves.addAll(rookMoveCalculator.pieceMoves(board, position));

        return validMoves;
    }
}