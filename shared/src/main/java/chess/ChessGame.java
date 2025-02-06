package chess;
import java.util.ArrayList;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessGame.TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        this.board = new ChessBoard();
        this.teamTurn = TeamColor.WHITE;
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
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
        // kings are different ?
        if (startPiece == null) { return null; }

        Collection<ChessMove> possibleMoves = startPiece.pieceMoves(board, startPosition);

        for (ChessMove move : possibleMoves) {
            if (isLegalMove(cloneChessBoard(board), move, startPiece.getTeamColor())){
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    private boolean isLegalMove(ChessBoard board, ChessMove move, TeamColor teamColor) {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());

        if (movingPiece == null) { return false; }

        // execute the move on the temporary board
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        board.addPiece(endPosition, movingPiece);
        board.addPiece(startPosition, null);

        return !isInCheckWithBoardCopy(teamColor, board); // need to make sure the board !isInCheck is looking at the boardCopy
    }

    private ChessBoard cloneChessBoard(ChessBoard board) {
        // can I do the cloning stuff here?
        ChessBoard newBoard = new ChessBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPosition pos = new ChessPosition(row + 1, col + 1);
                if (board.getPiece(pos) != null) {
                    ChessPiece piece = board.getPiece(pos);
                    newBoard.addPiece(pos, new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return newBoard;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException { // all on copied board
        // this should call valid moves - don't call make move inside of isLegalMove
        // should check whose turn it is
        // helper function to actually make the move, do error checking in makeMove

        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Invalid move: No piece at start position or wrong team's turn.");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        boolean isValid = false;

        for (ChessMove validMove : validMoves) {
            if (validMove.equals(move)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new InvalidMoveException("Invalid move: Move is not allowed.");
        }
        
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();

        ChessPiece.PieceType promotionType = move.getPromotionPiece();

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) { // i need to figure out how to do different promotion types
            if (piece.getTeamColor() == TeamColor.BLACK && endPosition.getRow() == 1) {
                ChessPiece promotionPiece = new ChessPiece(TeamColor.BLACK, promotionType);
                board.addPiece(endPosition, promotionPiece);
                board.addPiece(startPosition, null);
            } else if (piece.getTeamColor() == TeamColor.WHITE && endPosition.getRow() == 8) {
                ChessPiece promotionPiece = new ChessPiece(TeamColor.WHITE, promotionType);
                board.addPiece(endPosition, promotionPiece);
                board.addPiece(startPosition, null);
            } else {
                board.addPiece(endPosition, piece);
                board.addPiece(startPosition, null);
            }
        } else {
            board.addPiece(endPosition, piece);
            board.addPiece(startPosition, null);
        }

        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheckWithBoardCopy(TeamColor teamColor, ChessBoard boardCopy) { // Do I want this to reference board copy?
        ChessPosition kingPosition = findKingPosition(boardCopy, teamColor); // should I put board copy in the constructor?
        for (int row = 1; row <=8; row++) {
            for (int col = 1; col <=8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = boardCopy.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> possibleMoves = piece.pieceMoves(boardCopy, position);
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

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) { // Do I want this to reference board copy?
        ChessPosition kingPosition = findKingPosition(board, teamColor); // should I put board copy in the constructor?
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
        // fix
        if (!isInCheck(teamColor)) { return false; }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <=8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    // should I check for valid moves here?
                    Collection<ChessMove> validMoves = validMoves(position);
                    if (!validMoves.isEmpty()) return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // should check that they're not currently in check
        if (!isInCheck(teamColor)) {
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <=8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    // should I check for valid moves here?
                    Collection<ChessMove> validMoves = validMoves(position);
                    if (!validMoves.isEmpty()) return false;
                }
            }
        }
        return true;
    }

//    private boolean moveMakesKingBeInCheck(ChessMove move, ChessBoard board, TeamColor teamColor) {
//        ChessBoard newBoard = cloneChessBoard(board);
//        ChessPosition startPosition = move.getStartPosition();
//        ChessPosition endPosition = move.getEndPosition();
//        ChessPiece piece = board.getPiece(startPosition);
//        newBoard.addPiece(endPosition, piece);
//        newBoard.addPiece(startPosition, null);
//        return isInCheck(teamColor);
//    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
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
