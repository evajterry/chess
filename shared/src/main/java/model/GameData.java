package model;

import chess.ChessGame;

public record GameData(String whiteUsername, String blackUsername, int gameID, String gameName, ChessGame game) {
    public GameData updateWhiteUsername(String newWhiteUsername) {
        return new GameData(newWhiteUsername, this.blackUsername, this.gameID, this.gameName, this.game);
    }
    public GameData updateBlackUsername(String newBlackUsername) {
        return new GameData(this.whiteUsername, newBlackUsername, this.gameID, this.gameName, this.game);
    }
    public ChessGame getChessGame() {
        return this.game;
    }
}
