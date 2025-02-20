package dataaccess;

import model.GameData;
import java.util.HashMap;

public class GameAccess {
    private final HashMap<String, GameData> game = new HashMap<>();

    public void deleteAllData() {
        game.clear();
    }
}
