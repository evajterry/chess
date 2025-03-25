package client.apiclients;

public class JoinGameRequest {
    private String playerColor;
    private int gameID;

    public JoinGameRequest(int gameID, String playerColor) {
        this.gameID = gameID;
        this.playerColor = playerColor;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public int getGameID() {
        return gameID;
    }
}