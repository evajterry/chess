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
            whiteForwardMovement(validMoves, originalRow, originalCol, myPosition, board);
            whtDiagMove(validMoves, piece, originalRow, originalCol, myPosition, board);
        }
        if (teamColor == ChessGame.TeamColor.BLACK) {
            blFrwdMove(validMoves, originalRow, originalCol, myPosition, board);
            blDiagMove(validMoves, piece, originalRow, originalCol, myPosition, board);
        }

        return validMoves;
    }

    private void blDiagMove(Collection<ChessMove> vm, ChessPiece p, int r, int c, ChessPosition pos, ChessBoard board) {
        // right diagonal
        ChessPosition targetRight = new ChessPosition(r - 1, c + 1);
        if (isValidPosition(r - 1, c + 1)) {
            ChessPiece targetRightPiece = board.getPiece(targetRight);
            if (targetRightPiece != null) {
                ChessGame.TeamColor targetRightTeamColor = targetRightPiece.getTeamColor();
                ChessGame.TeamColor pieceColor = p.getTeamColor();
                if (pieceColor != targetRightTeamColor) {
                    if (r - 1 == 1) {
                        addPromotionPiece(vm, pos, targetRight);
                    } else {
                        vm.add(new ChessMove(pos, targetRight, null));
                    }
                }
            }
        }
        //left diagonal
        ChessPosition targetLeft = new ChessPosition(r - 1, c - 1);
        if (isValidPosition(r - 1, c - 1)) {
            ChessPiece targetLeftPiece = board.getPiece(targetLeft);
            if (targetLeftPiece != null) {
                ChessGame.TeamColor targetLeftTeamColor = targetLeftPiece.getTeamColor();
                ChessGame.TeamColor pieceColor = p.getTeamColor();
                if (pieceColor != targetLeftTeamColor) {
                    if (r - 1 == 1) {
                        addPromotionPiece(vm, pos, targetLeft);
                    } else {
                        vm.add(new ChessMove(pos, targetLeft, null));
                    }
                }
            }
        }
    }

    private void blFrwdMove(Collection<ChessMove> vm, int r, int c, ChessPosition pos, ChessBoard board) {
        // determine if it's the first move, add two
        ChessPosition targetPosition = new ChessPosition(r - 1, c);
        if (r == 7) {
            ChessPosition targetPositionPlus2 = new ChessPosition(r - 2, c);
            ChessPiece targetPiece = board.getPiece(targetPosition);
            ChessPiece targetPiecePlus2 = board.getPiece(targetPositionPlus2);

            if (targetPiece == null) {
                vm.add(new ChessMove(pos, targetPosition, null));
                if (targetPiecePlus2 == null) {
                    vm.add(new ChessMove(pos, targetPositionPlus2, null));
                }
            }// regular forward movement
        } else if (isValidPosition(r - 1, c)) {
            ChessPiece targetPiece = board.getPiece(targetPosition);
            if (targetPiece == null) {
                if (r - 1 == 1) {
                    addPromotionPiece(vm, pos, targetPosition);
                } else {
                    vm.add(new ChessMove(pos, targetPosition, null));
                }
            }
        }
    }

    private void whtDiagMove(Collection<ChessMove> vm, ChessPiece p, int r, int c, ChessPosition pos, ChessBoard board) {
        // right diagonal
        ChessPosition targetRight = new ChessPosition(r + 1, c + 1);
        if (isValidPosition(r + 1, c + 1)) {
            ChessPiece targetRightPiece = board.getPiece(targetRight);
            if (targetRightPiece != null) {
                ChessGame.TeamColor targetRightTeamColor = targetRightPiece.getTeamColor();
                ChessGame.TeamColor pieceColor = p.getTeamColor();
                if (pieceColor != targetRightTeamColor) {
                    if (r + 1 == 8) {
                        addPromotionPiece(vm, pos, targetRight);
                    } else {
                        vm.add(new ChessMove(pos, targetRight, null));
                    }
                }
            }
        }
        //left diagonal
        ChessPosition targetLeft = new ChessPosition(r + 1, c - 1);
        if (isValidPosition(r + 1, c - 1)) {
            ChessPiece targetLeftPiece = board.getPiece(targetLeft);
            if (targetLeftPiece != null) {
                ChessGame.TeamColor targetLeftTeamColor = targetLeftPiece.getTeamColor();
                ChessGame.TeamColor pieceColor = p.getTeamColor();
                if (pieceColor != targetLeftTeamColor) {
                    if (r + 1 == 8) {
                        addPromotionPiece(vm, pos, targetLeft);
                    } else {
                        vm.add(new ChessMove(pos, targetLeft, null));
                    }
                }
            }
        }
    }

    private void whiteForwardMovement(Collection<ChessMove> vM, int r, int c, ChessPosition pos, ChessBoard board) {
        // determine if it's the first move, add two
        ChessPosition targetPosition = new ChessPosition(r + 1, c);
        if (r == 2) {
            ChessPosition targetPositionPlus2 = new ChessPosition(r + 2, c);
            ChessPiece targetPiece = board.getPiece(targetPosition);
            ChessPiece targetPiecePlus2 = board.getPiece(targetPositionPlus2);

            if (targetPiece == null) {
                vM.add(new ChessMove(pos, targetPosition, null));
                if (targetPiecePlus2 == null) {
                    vM.add(new ChessMove(pos, targetPositionPlus2, null));
                }
            }// regular forward movement
        } else if (isValidPosition(r + 1, c)) {
            ChessPiece targetPiece = board.getPiece(targetPosition);
            if (targetPiece == null) {
                if (r + 1 == 8) {
                    addPromotionPiece(vM, pos, targetPosition);
                } else {
                    vM.add(new ChessMove(pos, targetPosition, null));
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
