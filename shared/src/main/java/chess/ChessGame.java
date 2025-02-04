package chess;
import java.util.ArrayList;
import java.util.Set;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private Set<TeamColor> turnTracking;
    private TeamColor team; // match to line in setTeamTurn
    private ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        if (turnTracking.contains(TeamColor.WHITE)) {
            return TeamColor.BLACK;

        } else if (turnTracking.contains(TeamColor.BLACK)) {
            return TeamColor.WHITE;
        } else {
            throw new RuntimeException("WHITE or BLACK not returned");
        }
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        if (getTeamTurn() == TeamColor.WHITE) {
            team = TeamColor.WHITE;
        } else if (getTeamTurn() == TeamColor.BLACK) {
            team = TeamColor.BLACK;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessBoard board = getBoard();
        ChessPiece startPiece = board.getPiece(startPosition);
        // kings are different
        if (startPiece == null) {
            return null;
        }

        Collection<ChessMove> possibleMoves = startPiece.pieceMoves(board, startPosition);

        for (ChessMove move : possibleMoves) {
            if (isLegalMove(board, move, startPiece.getTeamColor())){
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    private boolean isLegalMove(ChessBoard board, ChessMove move, TeamColor teamColor) {
        // copy board
        ChessBoard boardCopy = cloneChessBoard(board);
        // execute the move on the temporary board
        ChessPosition startPosition = move.getStartPosition();
        if (boardCopy.getPiece(startPosition) == null) {
            return false;
        } else if (boardCopy.getPiece(startPosition).getPieceType() == ChessPiece.PieceType.KING) {
            if (!isInCheck(teamColor) || !isInCheckmate(teamColor) || !isInStalemate(teamColor)) {
                return true;
            }
        } else {
            return false;
        }

        return !isInCheck(teamColor);
    }

    private ChessBoard cloneChessBoard(ChessBoard board) {
        // can I do the cloning stuff here?
        ChessBoard boardCopy = new ChessBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                if (board.getPiece(pos) != null) {
                    boardCopy.addPiece(pos, board.getPiece(pos));
                }
            }
        }
        return boardCopy;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition targetPosition =  move.getEndPosition();
        ChessPiece targetPiece = board.getPiece(targetPosition);
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());

        if (movingPiece != null) {
            board.addPiece(targetPosition, movingPiece);
            board.addPiece(move.getStartPosition(), null); // erases the moving piece from start pos
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(board, teamColor);
        for (int row = 1; row <=8; row++) {
            for (int col = 1; col <=8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> possibleMoves = piece.pieceMoves(board, position);
                    for (ChessMove move : possibleMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true; // a piece's end position is where the king is
                        }
                    }
                }
            }
        }
        return false;
    }

    private ChessPosition findKingPosition(ChessBoard board, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece newPiece = board.getPiece(newPosition);
                if (newPiece != null && newPiece.getTeamColor() == teamColor && newPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    return newPosition;
                }
            }
        }
        return null; // king is missing if this gets returned. where hast thou gone dear king
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
