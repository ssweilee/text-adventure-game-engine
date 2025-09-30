package edu.uob;

import java.util.Iterator;
import java.util.LinkedList;

public class Player {
    private final String name;
    private String location;
    private final EntityList inventory;
    private int health;

    public Player(String name, String location) {
        this.name = name;
        this.location = location;
        this.inventory = new EntityList();
        this.health = 3;
    }

    public String getName() { // 新增
        return this.name;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String loc) {
        this.location = loc;
    }

    public void addToInventory(GameEntity item) {
        this.inventory.add(item);
    }

    public String getInventory() {
        if (!this.inventory.hasEntities()) {
            return "Nothing";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<GameEntity> iterator = this.inventory.getEntities().iterator();
        while (iterator.hasNext()) {
            GameEntity item = iterator.next();
            sb.append(item.getDescription());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public boolean hasItem(String itemName) {
        return this.inventory.contains(itemName.toLowerCase());
    }

    public void removeFromInventory(String itemName) {
        this.inventory.remove(itemName);
    }

    public String getItemDescription(String itemName) {
        for (GameEntity item : this.inventory.getEntities()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item.getDescription();
            }
        }
        return null;
    }

    public int getHealth() {
        return this.health;
    }

    public void increaseHealth() {
        if (this.health < 3) {
            this.health++;
        }
    }

    public void decreaseHealth(Location currentLocation, String startLocation) {
        if (this.health > 0) {
            this.health--;
            if (this.health == 0) {
                this.dropAllItems(currentLocation);
                this.location = startLocation;
            }
        }
    }

    private void dropAllItems(Location currentLocation) {
        if (currentLocation == null) return;
        Iterator<GameEntity> iterator = this.inventory.getEntities().iterator();
        while (iterator.hasNext()) {
            GameEntity item = iterator.next();
            currentLocation.addArtefact(item);
            iterator.remove();
        }
    }

    public void resetHealth() {
        this.health = 3;
    }

    public LinkedList<GameEntity> getInventoryEntities() {
        return new LinkedList<>(this.inventory.getEntities());
    }
}