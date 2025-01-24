package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> CalculateMoves(ChessBoard board, ChessPosition position, ChessPiece.TeamColor team) {
        return new ArrayList<>();
    }
}
