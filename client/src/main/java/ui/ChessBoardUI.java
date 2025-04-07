package ui;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;

import static chess.ChessBoard.getPieceSymbol;
import static ui.EscapeSequences.*;

public class ChessBoardUI {
    // Board dimensions.
    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 1;
    private static final int LINE_WIDTH_IN_PADDED_CHARS = 1;
    private static final int HEADER_SPACING = 2; // Adjust as needed
    private static final ChessBoard BOARD = new ChessBoard();

    // Padded characters.
    private static final String EMPTY = "  ";

    public static void printChessBoard(String teamColor, Collection<ChessPosition> highlightedPositions) {
        System.out.println();
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        BOARD.resetBoard();

        out.print(ERASE_SCREEN);

        drawHeaders(out, teamColor);
        drawChessBoard(out, BOARD, teamColor, highlightedPositions);
        drawHeaders(out, teamColor);

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);

    }

    private static void drawHeaders(PrintStream out, String teamColor) {

        setBlack(out);
        out.print("   ");
        String[] headers = getHeadersByColor(teamColor);


        for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; ++boardCol) {
            drawHeader(out, headers[boardCol]);

            if (boardCol < BOARD_SIZE_IN_SQUARES - 1) {
                out.print("   "); // space between letters in hdr
            }
        }
        out.println();
    }

    private static String[] getHeadersByColor(String teamColor) {
        if (Objects.equals(teamColor, "WHITE")) {
            return new String[]{"a", "b", "c", "d", "e", "f", "g", "h"};
        } else {
            return new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};
        }
    }

    private static void drawHeader(PrintStream out, String headerText) {
        int prefixLength = 0;
        int suffixLength = 0;

        out.print(EMPTY.repeat(prefixLength));
        printHeaderText(out, headerText);
        out.print(EMPTY.repeat(suffixLength));
    }

    private static void printHeaderText(PrintStream out, String player) {
        out.print(" " + SET_TEXT_COLOR_WHITE);

        out.print(player);

        setBlack(out);
    }

    private static void drawChessBoard(
            PrintStream out, ChessBoard chessBoard, String teamColor, Collection<ChessPosition> highlightedPositions) {
        for (int boardRow = 0; boardRow < BOARD_SIZE_IN_SQUARES; ++boardRow) {

            drawRowOfSquares(out, boardRow, chessBoard, teamColor, highlightedPositions);

            if (boardRow < BOARD_SIZE_IN_SQUARES - 1) {
                setBlack(out);
            }
        }
    }

    private static void drawRowOfSquares(
            PrintStream out, int row, ChessBoard chessBoard, String teamColor, Collection<ChessPosition> highlightedPositions) {
        int displayRow = teamColor.equalsIgnoreCase("BLACK") ? 7 - row : row;
        for (int squareRow = 0; squareRow < SQUARE_SIZE_IN_PADDED_CHARS; ++squareRow) {
            if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS / 2) {
                drawRowHeader(out, 8 - displayRow, teamColor);
            } else {
                out.print("   ");
            }

            for (int col = 0; col < BOARD_SIZE_IN_SQUARES; ++col) {
                int displayCol = teamColor.equalsIgnoreCase("BLACK") ? 7 - col : col;

                ChessPosition currentPosition = new ChessPosition(displayRow + 1, displayCol + 1);
                boolean isHighlighted = highlightedPositions != null && highlightedPositions.contains(currentPosition);

                setSquareColor(out, displayRow, displayCol, isHighlighted);
                if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS / 2) {
                    ChessPiece piece = chessBoard.getPiece(new ChessPosition(displayRow + 1, displayCol + 1));
                    String pieceSymbol = (piece != null) ? getPieceSymbol(piece) : " ";
                    out.print("  " + pieceSymbol + "  ");
                } else {
                    out.print("     "); // Empty space
                }
                setBlack(out); // Reset color after each square
            }

            if (squareRow == SQUARE_SIZE_IN_PADDED_CHARS / 2) {
                drawRowHeader(out, 8 - displayRow, teamColor); // Print row number on the right
            } else {
                out.print("   "); // Maintain alignment for empty rows
            }

            System.out.println();
        }
    }

    private static void drawRowHeader(PrintStream out, int rowNum, String teamColor) {
        setBlack(out);
        out.print(SET_TEXT_COLOR_WHITE);
        out.print(" " + rowNum + " ");
    }

    private static void setSquareColor(PrintStream out, int row, int col, boolean isHighlighted) {
        if (isHighlighted) {
            out.print(SET_BG_COLOR_DARK_GREEN);  // Use the escape sequence for light blue here
        } else {
            if ((row + col) % 2 == 0) {
                out.print(SET_BG_COLOR_WHITE);
            } else {
                out.print(SET_BG_COLOR_DARK_GREY);
            }
        }
        if (row == 0 || row == 1) {
            out.print(SET_TEXT_COLOR_MAGENTA);
        } else if (row == 6 || row == 7) {
            out.print(SET_TEXT_COLOR_BLUE);
        }
    }


    private static void setBlack(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

}
