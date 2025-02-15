package model;

import chess.ChessGame;

public record GameData(String whiteUsername, String blackUsername, int gameID, String gameName, ChessGame game) {
}
