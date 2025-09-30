package edu.uob;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

public class GameInitialiser {
    private final GameState gameState;

    public GameInitialiser(GameState gameState) {
        this.gameState = gameState;
    }

    public void initialise(File entitiesFile, File actionsFile) {
        EntityParser entityParser = new EntityParser(this.gameState);
        entityParser.parseEntities(entitiesFile);
        Location storeroom = this.gameState.getStoreroom();
        if (!this.gameState.getLocations().containsKey("storeroom")) {
            this.gameState.addLocation("storeroom", "");
        }
        LinkedList<GameAction> actions = new LinkedList<>();
        ActionParser actionParser = new ActionParser(actions);
        actionParser.parseAction(actionsFile);
        Iterator<GameAction> actionIterator = actions.iterator();
        while (actionIterator.hasNext()) {
            GameAction action = actionIterator.next();
            this.gameState.addAction(action);
        }
    }
}