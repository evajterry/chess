package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface ChessPieceMovesCalculator {
    /**
     * Calculates all valid moves for a piece on the board at a given position.
     *
     * @param board     The current chessboard state
     * @param position  The position of the piece to calculate moves for
     * @return A collection of valid moves
     */
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}