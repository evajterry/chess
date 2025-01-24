package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator implements ChessPieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        ChessPiece piece = board.getPiece(position);
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ChessGame.TeamColor teamColor = piece.getTeamColor();

        int row = position.getRow();
        int col = position.getColumn();

        if (teamColor == ChessGame.TeamColor.BLACK) {
            calculateBlackPawnMoves(board, position, piece, possibleMoves, row, col);

        } else if (teamColor == ChessGame.TeamColor.WHITE) {
            calculateWhitePawnMoves(board, position, piece, possibleMoves, row, col);
        }

        return possibleMoves;
    }

    private void calculateWhitePawnMoves(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col) {
        forwardWhiteMovement(board, position, piece, possibleMoves, row, col);
        diagonalAttackMovement(board, position, piece, possibleMoves, row, col);
    }

    private void diagonalAttackMovement(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col) {
        ChessPosition upperRightPosition = new ChessPosition(row + 1, col + 1);
        ChessPosition upperLeftPosition = new ChessPosition(row + 1, col - 1);

        if(diagonalRightMovementPromotion(board, position, piece, possibleMoves, row, col, upperRightPosition)) {
            setPromotionPiece(position, upperRightPosition, possibleMoves);
        } else if (diagonalRightMovement(board, position, piece, possibleMoves, row, col, upperRightPosition)) {
            possibleMoves.add(new ChessMove(position, upperRightPosition, null));
        }

        if(diagonalLeftMovementPromotion(board, position, piece, possibleMoves, row, col, upperLeftPosition)) {
            setPromotionPiece(position, upperLeftPosition, possibleMoves);
        } else if (diagonalLeftMovement(board, position, piece, possibleMoves, row, col, upperLeftPosition)) {
            possibleMoves.add(new ChessMove(position, upperLeftPosition, null));
        }
    }

    private boolean diagonalLeftMovement(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col, ChessPosition upperLeftPosition) {
        if (isPositionValid(row + 1, col - 1)) { // check this
            ChessPiece promotionPiece = board.getPiece(upperLeftPosition);
            if (promotionPiece != null && promotionPiece.getPieceType() != null) {
                ChessGame.TeamColor attackingColor = piece.getTeamColor();
                return (board.getPiece(upperLeftPosition).getTeamColor() != attackingColor); // check this
            }
        }
        return false;
    }

    private boolean diagonalLeftMovementPromotion(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col, ChessPosition upperLeftPosition) {
        if (isPositionValid(row - 1, col - 1)) {
            ChessPiece promotionPiece = board.getPiece(upperLeftPosition);
            if (promotionPiece != null) {
                ChessGame.TeamColor attackingColor = piece.getTeamColor();
                if (board.getPiece(upperLeftPosition).getTeamColor() != attackingColor) { // check this
                    // check if it needs to be promoted too
                    return row == 7;
                }
            }
        }
        return false;
    }

    private boolean diagonalRightMovementPromotion(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col, ChessPosition upperRightPosition) {
//        ChessPiece promotionPiece = board.getPiece(upperRightPosition);
        if (isPositionValid(row + 1, col + 1)) {
            ChessPiece promotionPiece = board.getPiece(upperRightPosition);
            if (promotionPiece != null && promotionPiece.getPieceType() != null) {
                ChessGame.TeamColor attackingColor = piece.getTeamColor();
                if (board.getPiece(upperRightPosition).getTeamColor() != attackingColor) { // check this
                    // check if it needs to be promoted too
                    return row == 7;
                }
            }
        }
        return false;
    }

    private boolean diagonalRightMovement(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col, ChessPosition upperRightPosition) {
        if (isPositionValid(row + 1, col + 1)) {
            ChessPiece promotionPiece = board.getPiece(upperRightPosition);
            if (promotionPiece != null && promotionPiece.getPieceType() != null) {
                ChessGame.TeamColor attackingColor = piece.getTeamColor();
                return (board.getPiece(upperRightPosition).getTeamColor() != attackingColor); // check this
            }
        }
        return false;
    }


    private void forwardWhiteMovement(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int column) {
        if (isPositionValid(row - 1, column - 1)) {
            ChessPosition newPosition = new ChessPosition(row + 1, column);
            ChessPiece newPiece = board.getPiece(newPosition);
            // check if the piece has moved before
            if (row == 2 && newPiece == null) {
                ChessPosition newPositionPlusOne = new ChessPosition(row + 2, column);
                possibleMoves.add(new ChessMove(position, newPosition, null));

                ChessPiece newPiecePlusOne = board.getPiece(newPositionPlusOne);
                if (newPiecePlusOne == null) {
                    possibleMoves.add(new ChessMove(position, newPositionPlusOne, null));
                }
            } else if (isPositionValid(row, column - 1) && newPiece == null) { // check if it's made it across the board
                if (row == 7) {
                    setPromotionPiece(position, newPosition, possibleMoves);
                } else { // move one forward
                    possibleMoves.add(new ChessMove(position, newPosition, null));
                }
            }
        }
    }

    private void setPromotionPiece(ChessPosition position, ChessPosition newPosition, Collection<ChessMove> possibleMoves) {
        possibleMoves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.QUEEN));
        possibleMoves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.ROOK));
        possibleMoves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.BISHOP));
        possibleMoves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.KNIGHT));
    }

    private void calculateBlackPawnMoves(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col) {
        forwardBlackMovement(board, position, piece, possibleMoves, row, col);
        diagonalBlackAttackMovement(board, position, piece, possibleMoves, row, col);
    }

    private void diagonalBlackAttackMovement(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col) {
        ChessPosition lowerRightPosition = new ChessPosition(row - 1, col + 1);
        ChessPosition lowerLeftPosition = new ChessPosition(row - 1, col - 1);

        if(blackDiagonalRightMovementPromotion(board, position, piece, possibleMoves, row, col, lowerRightPosition)) {
            setPromotionPiece(position, lowerRightPosition, possibleMoves);
        } else if (blackDiagonalRightMovement(board, position, piece, possibleMoves, row, col, lowerRightPosition)) {
            possibleMoves.add(new ChessMove(position, lowerRightPosition, null));
        }

        if(blackDiagonalLeftMovementPromotion(board, position, piece, possibleMoves, row, col, lowerLeftPosition)) {
            setPromotionPiece(position, lowerLeftPosition, possibleMoves);
        } else if (blackDiagonalLeftMovement(board, position, piece, possibleMoves, row, col, lowerLeftPosition)) {
            possibleMoves.add(new ChessMove(position, lowerLeftPosition, null));
        }

    }

    private boolean blackDiagonalLeftMovement(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col, ChessPosition lowerLeftPosition) {
        if (isPositionValid(row - 1, col - 1)) { // check this
            ChessPiece promotionPiece = board.getPiece(lowerLeftPosition);
            if (promotionPiece != null && promotionPiece.getPieceType() != null) {
                ChessGame.TeamColor attackingColor = piece.getTeamColor();
                return (board.getPiece(lowerLeftPosition).getTeamColor() != attackingColor); // check this
            }
        }
        return false;
    }

    private boolean blackDiagonalLeftMovementPromotion(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col, ChessPosition lowerLeftPosition) {
        if (isPositionValid(row - 1, col - 1)) {
            ChessPiece promotionPiece = board.getPiece(lowerLeftPosition);
            if (promotionPiece != null) {
                ChessGame.TeamColor attackingColor = piece.getTeamColor();
                if (board.getPiece(lowerLeftPosition).getTeamColor() != attackingColor) { // check this
                    // check if it needs to be promoted too
                    return row == 2;
                }
            }
        }
        return false;
    }

    private boolean blackDiagonalRightMovement(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col, ChessPosition lowerRightPosition) {
        if (isPositionValid(row - 1, col + 1)) {
            ChessPiece promotionPiece = board.getPiece(lowerRightPosition);
            if (promotionPiece != null && promotionPiece.getPieceType() != null) {
                ChessGame.TeamColor attackingColor = piece.getTeamColor();
                return (board.getPiece(lowerRightPosition).getTeamColor() != attackingColor); // check this
            }
        }
        return false;
    }

    private boolean blackDiagonalRightMovementPromotion(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col, ChessPosition lowerRightPosition) {
        if (isPositionValid(row - 1, col + 1)) {
            ChessPiece promotionPiece = board.getPiece(lowerRightPosition);
            if (promotionPiece != null && promotionPiece.getPieceType() != null) {
                ChessGame.TeamColor attackingColor = piece.getTeamColor();
                if (board.getPiece(lowerRightPosition).getTeamColor() != attackingColor) { // check this
                    // check if it needs to be promoted too
                    return row == 0;
                }
            }
        }
        return false;
    }

    private void forwardBlackMovement(ChessBoard board, ChessPosition position, ChessPiece piece, Collection<ChessMove> possibleMoves, int row, int col) {
        if (isPositionValid(row - 1, col - 1)) {
            ChessPosition newPosition = new ChessPosition(row - 1, col);
            ChessPiece newPiece = board.getPiece(newPosition);
            // check if the piece has moved before
            if (row == 7 && newPiece == null) {
                ChessPosition newPositionPlusOne = new ChessPosition(row - 2, col);
                possibleMoves.add(new ChessMove(position, newPosition, null));

                ChessPiece newPiecePlusOne = board.getPiece(newPositionPlusOne);
                if (newPiecePlusOne == null) {
                    possibleMoves.add(new ChessMove(position, newPositionPlusOne, null));
                }
            } else if (isPositionValid(row - 2, col - 1) && newPiece == null) { // check if it's made it across the board
                if (row == 2) {
                    setPromotionPiece(position, newPosition, possibleMoves);
                } else { // move one forward
                    possibleMoves.add(new ChessMove(position, newPosition, null));
                }
            }
        }
    }

    private boolean isPositionValid(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
}
