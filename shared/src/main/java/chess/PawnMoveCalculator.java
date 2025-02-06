package chess;
import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator implements ChessPieceMovesCalculator {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        int originalRow = myPosition.getRow();
        int originalCol = myPosition.getColumn();

        if (teamColor == ChessGame.TeamColor.WHITE) {
            whiteForwardMovement(validMoves, piece, originalRow, originalCol, myPosition, board);
            whiteDiagonalMovement(validMoves, piece, originalRow, originalCol, myPosition, board);
        }
        if (teamColor == ChessGame.TeamColor.BLACK) {
            blackForwardMovement(validMoves, piece, originalRow, originalCol, myPosition, board);
            blackDiagonalMovement(validMoves, piece, originalRow, originalCol, myPosition, board);
        }

        return validMoves;
    }

    private void blackDiagonalMovement(Collection<ChessMove> validMoves, ChessPiece piece, int originalRow, int originalCol, ChessPosition myPosition, ChessBoard board) {
        // right diagonal
        ChessPosition targetRight = new ChessPosition(originalRow - 1, originalCol + 1);
        if (isValidPosition(originalRow - 1, originalCol + 1)) {
            ChessPiece targetRightPiece = board.getPiece(targetRight);
            if (targetRightPiece != null) {
                ChessGame.TeamColor targetRightTeamColor = targetRightPiece.getTeamColor();
                ChessGame.TeamColor pieceColor = piece.getTeamColor();
                if (pieceColor != targetRightTeamColor) {
                    if (originalRow - 1 == 1) {
                        addPromotionPiece(validMoves, myPosition, targetRight);
                    } else {
                        validMoves.add(new ChessMove(myPosition, targetRight, null));
                    }
                }
            }
        }
        //left diagonal
        ChessPosition targetLeft = new ChessPosition(originalRow - 1, originalCol - 1);
        if (isValidPosition(originalRow - 1, originalCol - 1)) {
            ChessPiece targetLeftPiece = board.getPiece(targetLeft);
            if (targetLeftPiece != null) {
                ChessGame.TeamColor targetLeftTeamColor = targetLeftPiece.getTeamColor();
                ChessGame.TeamColor pieceColor = piece.getTeamColor();
                if (pieceColor != targetLeftTeamColor) {
                    if (originalRow - 1 == 1) {
                        addPromotionPiece(validMoves, myPosition, targetLeft);
                    } else {
                        validMoves.add(new ChessMove(myPosition, targetLeft, null));
                    }
                }
            }
        }
    }

    private void blackForwardMovement(Collection<ChessMove> validMoves, ChessPiece piece, int originalRow, int originalCol, ChessPosition myPosition, ChessBoard board) {
        // determine if it's the first move, add two
        ChessPosition targetPosition = new ChessPosition(originalRow - 1, originalCol);
        if (originalRow == 7) {
            ChessPosition targetPositionPlus2 = new ChessPosition(originalRow - 2, originalCol);
            ChessPiece targetPiece = board.getPiece(targetPosition);
            ChessPiece targetPiecePlus2 = board.getPiece(targetPositionPlus2);

            if (targetPiece == null) {
                validMoves.add(new ChessMove(myPosition, targetPosition, null));
                if (targetPiecePlus2 == null) {
                    validMoves.add(new ChessMove(myPosition, targetPositionPlus2, null));
                }
            }// regular forward movement
        } else if (isValidPosition(originalRow - 1, originalCol)) {
            ChessPiece targetPiece = board.getPiece(targetPosition);
            if (targetPiece == null) {
                if (originalRow - 1 == 1) {
                    addPromotionPiece(validMoves, myPosition, targetPosition);
                } else {
                    validMoves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
    }

    private void whiteDiagonalMovement(Collection<ChessMove> validMoves, ChessPiece piece, int originalRow, int originalCol, ChessPosition myPosition, ChessBoard board) {
        // right diagonal
        ChessPosition targetRight = new ChessPosition(originalRow + 1, originalCol + 1);
        if (isValidPosition(originalRow + 1, originalCol + 1)) {
            ChessPiece targetRightPiece = board.getPiece(targetRight);
            if (targetRightPiece != null) {
                ChessGame.TeamColor targetRightTeamColor = targetRightPiece.getTeamColor();
                ChessGame.TeamColor pieceColor = piece.getTeamColor();
                if (pieceColor != targetRightTeamColor) {
                    if (originalRow + 1 == 8) {
                        addPromotionPiece(validMoves, myPosition, targetRight);
                    } else {
                        validMoves.add(new ChessMove(myPosition, targetRight, null));
                    }
                }
            }
        }
        //left diagonal
        ChessPosition targetLeft = new ChessPosition(originalRow + 1, originalCol - 1);
        if (isValidPosition(originalRow + 1, originalCol - 1)) {
            ChessPiece targetLeftPiece = board.getPiece(targetLeft);
            if (targetLeftPiece != null) {
                ChessGame.TeamColor targetLeftTeamColor = targetLeftPiece.getTeamColor();
                ChessGame.TeamColor pieceColor = piece.getTeamColor();
                if (pieceColor != targetLeftTeamColor) {
                    if (originalRow + 1 == 8) {
                        addPromotionPiece(validMoves, myPosition, targetLeft);
                    } else {
                        validMoves.add(new ChessMove(myPosition, targetLeft, null));
                    }
                }
            }
        }
    }

    private void whiteForwardMovement(Collection<ChessMove> validMoves, ChessPiece piece, int originalRow, int originalCol, ChessPosition myPosition, ChessBoard board) {
        // determine if it's the first move, add two
        ChessPosition targetPosition = new ChessPosition(originalRow + 1, originalCol);
        if (originalRow == 2) {
            ChessPosition targetPositionPlus2 = new ChessPosition(originalRow + 2, originalCol);
            ChessPiece targetPiece = board.getPiece(targetPosition);
            ChessPiece targetPiecePlus2 = board.getPiece(targetPositionPlus2);

            if (targetPiece == null) {
                validMoves.add(new ChessMove(myPosition, targetPosition, null));
                if (targetPiecePlus2 == null) {
                    validMoves.add(new ChessMove(myPosition, targetPositionPlus2, null));
                }
            }// regular forward movement
        } else if (isValidPosition(originalRow + 1, originalCol)) {
            ChessPiece targetPiece = board.getPiece(targetPosition);
            if (targetPiece == null) {
                if (originalRow + 1 == 8) {
                    addPromotionPiece(validMoves, myPosition, targetPosition);
                } else {
                    validMoves.add(new ChessMove(myPosition, targetPosition, null));
                }
            }
        }
    }

    private void addPromotionPiece(Collection<ChessMove> validMoves, ChessPosition myPosition, ChessPosition targetPosition) {
        validMoves.add(new ChessMove(myPosition, targetPosition, ChessPiece.PieceType.QUEEN));
        validMoves.add(new ChessMove(myPosition, targetPosition, ChessPiece.PieceType.ROOK));
        validMoves.add(new ChessMove(myPosition, targetPosition, ChessPiece.PieceType.BISHOP));
        validMoves.add(new ChessMove(myPosition, targetPosition, ChessPiece.PieceType.KNIGHT));
    }

    private boolean isValidPosition(int row, int col) {
        return row > 0 && col > 0 && row <= 8 && col <= 8;
    }
}
