package chess;

import java.util.Arrays;
import java.util.Objects;

import static chess.ChessPiece.PieceType.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        printChessBoard(this);
        System.out.print("\n\n");
        printChessBoard(that);
        System.out.print("\n\n");
        return Objects.deepEquals(board, that.board);
    } // double for loop, iterate through row columns, check if this rook == that rook,

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int row = position.getRow();
        int col = position.getColumn();
        if (row > 0 && row <= 8 && col > 0 && col <= 8) {
            // Place the piece at the new position
            board[row - 1][col - 1] = piece;
        }
        // maybe should add something that removes the piece from its previous spot?
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1]; // maybe get rid of the -1
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        this.board = new ChessPiece[8][8]; // I think this line is messing it up
        for (int col = 0; col < 8; col++) {
            board[1][col] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            board[6][col] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
        }
        // rooks
        board[0][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ROOK);
        board[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ROOK);

        board[7][0] = new ChessPiece(ChessGame.TeamColor.BLACK, ROOK);
        board[7][7] = new ChessPiece(ChessGame.TeamColor.BLACK, ROOK);

        // knights
        board[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        board[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);

        board[7][1] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        board[7][6] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);

        // bishops
        board[0][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        board[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);

        board[7][2] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        board[7][5] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);

        // Queens
        board[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, QUEEN);
        board[7][3] = new ChessPiece(ChessGame.TeamColor.BLACK, QUEEN);

        // Kings
        board[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, KING);
        board[7][4] = new ChessPiece(ChessGame.TeamColor.BLACK, KING);
    }

    public static void printChessBoard(ChessBoard chessBoard) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row + 1, col + 1));
                if (piece != null) {
                    System.out.print(getPieceSymbol(piece) + " ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }

    /**
     * Converts a chess piece to a printable symbol.
     *
     * @param piece The chess piece to convert.
     * @return The symbol representing the piece.
     */
    public static String getPieceCharacter(ChessPiece piece) {
        String symbol = "";
        switch (piece.getPieceType()) {
            case PAWN:
                symbol = "P";
                break;
            case ROOK:
                symbol = "R";
                break;
            case KNIGHT:
                symbol = "N";
                break;
            case BISHOP:
                symbol = "B";
                break;
            case QUEEN:
                symbol = "Q";
                break;
            case KING:
                symbol = "K";
                break;
        }
        return piece.getTeamColor() == ChessGame.TeamColor.BLACK ? symbol.toLowerCase() : symbol;
    }

    public static String getPieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? "♔" : "♚";
            case QUEEN -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? "♕" : "♛";
            case ROOK -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? "♖" : "♜";
            case BISHOP -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? "♗" : "♝";
            case KNIGHT -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? "♘" : "♞";
            case PAWN -> (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? "♙" : "♟";
        };
    }

}