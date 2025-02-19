package dataaccess;

import model.GameData;
import java.util.HashMap;

public class GameAccess implements UserDAO{
    private final HashMap<String, GameData> game = new HashMap<>();

    @Override
    public void deleteAllData() {
        game.clear();
    }
}
