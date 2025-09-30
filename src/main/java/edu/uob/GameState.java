package edu.uob;

import java.util.*;

public class GameState {
    private final Map<String, Player> players;
    private final Map<String, Location> locations;
    private final HashMap<String, LinkedList<GameAction>> actionsByTrigger;
    private final String startLocation;
    private final Location storeroom;

    public GameState(String initialLocation) {
        this.players = new HashMap<>();
        this.startLocation = initialLocation;
        this.locations = new HashMap<>();
        this.actionsByTrigger = new HashMap<>();
        this.storeroom = new Location("storeroom", "");
        this.locations.put("storeroom", this.storeroom);
    }

    public Location getStoreroom() {
        return this.storeroom;
    }

    public void addPlayer(String playerName) {
        Player newPlayer = new Player(playerName, this.startLocation);
        this.players.put(playerName, newPlayer);
    }

    public void addLocation(String name, String description) {
        this.locations.put(name, new Location(name, description));
    }

    public Location getLocation(String name) {
        return this.locations.get(name);
    }

    public Map<String, Location> getLocations() {
        return this.locations;
    }

    public void addPath(String from, String to) {
        Location fromLoc = this.locations.get(from);
        if (fromLoc != null) {
            fromLoc.addPath(to);
        }
    }

    public String look(String playerName) {
        Player player = this.players.get(playerName);
        if (player == null) {
            return this.playerNotFoundMessage(playerName);
        }
        Location loc = this.locations.get(player.getLocation());
        StringBuilder desc = new StringBuilder();
        desc.append("You are in ");
        desc.append(loc.getDescription());
        desc.append(".\n");
        this.appendEntitiesDescription(loc, playerName, desc);
        return desc.toString();
    }

    private String playerNotFoundMessage(String playerName) {
        StringBuilder playerNotFoundMessage = new StringBuilder();
        playerNotFoundMessage.append("Player ").append(playerName).append(" not found");
        return playerNotFoundMessage.toString();
    }

    private void appendEntitiesDescription(Location loc, String playerName, StringBuilder desc) {
        this.appendIfNotEmpty(loc.getArtefacts(), "You can see: \n", desc);
        if (!loc.getArtefacts().isEmpty()) {
            desc.append("\n");
        }
        if (!loc.getFurniture().isEmpty()) {
            this.appendIfNotEmpty(loc.getFurniture(), "", desc);
            desc.append("\n");
        }
        if (!loc.getCharacters().isEmpty()) {
            this.appendIfNotEmpty(loc.getCharacters(), "", desc);
            desc.append("\n");
        }
        this.appendOtherPlayers(loc, playerName, desc);
        this.appendIfNotEmpty(loc.getPaths(), "You can access from here: \n", desc);
    }

    private void appendOtherPlayers(Location loc, String playerName, StringBuilder desc) {
        StringBuilder otherPlayers = new StringBuilder();
        for (Player p : this.players.values()) {
            if (!p.getName().equals(playerName) && p.getLocation().equals(loc.getName())) {
                if (otherPlayers.length() > 0) {
                    otherPlayers.append(", ");
                }
                otherPlayers.append(p.getName());
            }
        }
        if (!otherPlayers.isEmpty()) {
            this.appendIfNotEmpty(otherPlayers.toString(), "Other players here: \n", desc);
            desc.append("\n");
        }

    }

    public String getItem(String playerName, String item) {
        Player player = this.players.get(playerName);
        if (player == null) return this.playerNotFoundMessage(playerName);
        Location loc = this.locations.get(player.getLocation());
        if (loc.hasArtefact(item)) {
            GameEntity artefact = loc.getArtefactEntity(item);
            if (!(artefact instanceof Artefact)) {
                StringBuilder warningMessage = new StringBuilder();
                warningMessage.append("Cannot pick up: ");
                warningMessage.append(item);
                return warningMessage.toString();
            }
            player.addToInventory(artefact);
            loc.removeArtefact(item);
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("You picked up ");
            messageBuilder.append(artefact.getName());
            return messageBuilder.toString();
        }
        if (loc.hasFurniture(item)) {
            StringBuilder warningMessage = new StringBuilder();
            warningMessage.append("Cannot pick up furniture: ");
            warningMessage.append(item);
            return warningMessage.toString();
        }
        if (loc.hasCharacter(item)) {
            StringBuilder warningMessage = new StringBuilder();
            warningMessage.append("Cannot pick up character: ");
            warningMessage.append(item);
            return warningMessage.toString();
        }
        return "No such item here";
    }

    public String dropItem(String playerName, String item) {
        Player player = this.players.get(playerName);
        Location loc = this.locations.get(player.getLocation());
        if (player.hasItem(item)) {
            GameEntity artefact = new Artefact(item, player.getItemDescription(item));
            player.removeFromInventory(item);
            loc.addArtefact(artefact);
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("You dropped ");
            messageBuilder.append(item);
            return messageBuilder.toString();
        }
        return "No such item in inventory";
    }

    public String gotoLocation(String playerName, String destination) {
        Player player = this.players.get(playerName);
        Location current = this.locations.get(player.getLocation());
        if (player.getLocation().equalsIgnoreCase(destination)) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("You are already in ");
            messageBuilder.append(destination);
            return messageBuilder.toString();
        }

        if (current.hasPath(destination)) {
            player.setLocation(destination);
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("You moved to ");
            messageBuilder.append(destination);
            return messageBuilder.toString();
        }
        return "No path to that location";
    }

    public String inventory(String playerName) {
        Player player = this.players.get(playerName);
        return player.getInventory();
    }

    public String executeAction(String playerName, GameAction action) {
        Player player = this.players.get(playerName);
        Location currentLocation = this.locations.get(player.getLocation());
        Location storeroom = this.getStoreroom();

        if (player == null || currentLocation == null) {
            return "Player or location not found";
        }

        for (String subject : action.getSubjects()) {
            if (!isSubjectAvailable(subject, currentLocation, player)) {
                return this.checkSubjectAvailabilityWithOtherPlayers(subject, playerName);
            }
        }
        String narration = action.getNarration();
        this.consumeEntities(action.getConsumed(), player, currentLocation, storeroom);
        this.produceEntities(action.getProduced(), currentLocation, storeroom, player);

        StringBuilder result = new StringBuilder();
        result.append(narration);
        if (player.getHealth() == 0) {
            result.append("\nYou died and lost all of your items, you will return to the start location of the game");
            player.resetHealth();
        }
        return result.toString();
    }
    private boolean isSubjectAvailable(String subject, Location loc, Player player) {
        return loc.hasArtefact(subject) || loc.hasFurniture(subject) || loc.hasCharacter(subject) || player.hasItem(subject);
    }

    private String checkSubjectAvailabilityWithOtherPlayers(String subject, String playerName) {
        for (Player other : this.players.values()) {
            if (!other.getName().equals(playerName) && other.hasItem(subject)) {
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Subject '").append(subject).append("' is not available");
                return messageBuilder.toString();
            }
        }
        return null;
    }

    private void consumeEntities(List<String> consumed, Player player, Location loc, Location storeroom) {
        for (String item : consumed) {
            if (!item.isEmpty()) {
                if (item.equals("health")) {
                    player.decreaseHealth(loc, this.startLocation);
                } else if (this.locations.containsKey(item)) {
                    loc.removePath(item);
                } else if (loc.hasArtefact(item)) {
                    loc.removeArtefact(item);
                    if (storeroom != null) {
                        String itemName = item;
                        String description = String.format("A %s", itemName);
                        Artefact artefact = new Artefact(itemName, description);
                        storeroom.addArtefact(artefact);
                    }
                } else if (loc.hasFurniture(item)) {
                    loc.removeFurniture(item);
                } else if (player.hasItem(item)) {
                    player.removeFromInventory(item);
                    if (storeroom != null) {
                        String itemName = item;
                        String description = String.format("A %s", itemName);
                        Artefact artefact = new Artefact(itemName, description);
                        storeroom.addArtefact(artefact);
                    }

                }
            }
        }
    }

    private void produceEntities(List<String> produced, Location loc, Location storeroom, Player player) {
        for (String item : produced) {
            if (!item.isEmpty()) {
                if (item.equals("health")) {
                    player.increaseHealth();
                } else if (this.locations.containsKey(item)) {
                    loc.addPath(item);
                } else {
                    GameEntity entity = null;
                    if (storeroom != null) {
                        if (storeroom.hasArtefact(item)) {
                            entity = storeroom.getArtefactEntity(item);
                            storeroom.removeArtefact(item);
                            loc.addArtefact(entity);
                        } else if (storeroom.hasFurniture(item)) {
                            entity = storeroom.getFurnitureEntity(item);
                            storeroom.removeFurniture(item);
                            loc.addFurniture(entity);
                        } else if (storeroom.hasCharacter(item)) {
                            entity = storeroom.getCharacterEntity(item);
                            storeroom.removeCharacter(item);
                            loc.addCharacter(entity);
                        }
                    }
                    if (entity == null) {
                        StringBuilder desc = new StringBuilder();
                        desc.append("A ").append(item);
                        entity = new Artefact(item, desc.toString());
                        loc.addArtefact(entity);
                    }
                }
            }
        }
    }

    private void appendIfNotEmpty(String content, String prefix, StringBuilder desc) {
        if (!content.isEmpty()) {
            desc.append(prefix);
            desc.append(content);
        }
    }

    public Player getPlayer(String name) {
        return this.players.get(name);
    }

    public Map<String, Player> getPlayers() {
        return this.players;
    }

    public void addAction(GameAction action) {
        Iterator<String> triggerIterator = action.getTriggers().iterator();
        while (triggerIterator.hasNext()) {
            String trigger = triggerIterator.next();
            LinkedList<GameAction> actionList = this.actionsByTrigger.get(trigger);
            if (actionList == null) {
                actionList = new LinkedList<>();
                this.actionsByTrigger.put(trigger, actionList);
            }
            actionList.add(action);
        }
    }

    public LinkedList<GameAction> getActionsByTrigger(String trigger) {
        return this.actionsByTrigger.getOrDefault(trigger, new LinkedList<>());
    }

    public LinkedList<String> getAllTriggers() {
        return new LinkedList<>(this.actionsByTrigger.keySet());
    }
}