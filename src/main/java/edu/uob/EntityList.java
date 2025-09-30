package edu.uob;

import java.util.HashSet;

public class EntityList {
    private final HashSet<GameEntity> entities;

    public EntityList() {
        this.entities = new HashSet<>();
    }

    public void add(GameEntity entity) {
        this.entities.add(entity);
    }

    public void remove(String name) {
        GameEntity toRemove = null;
        for (GameEntity entity : this.entities) {
            if (entity.getName().equals(name)) {
                toRemove = entity;
                break;
            }
        }
        if (toRemove != null) {
            this.entities.remove(toRemove);
        }
    }

    public boolean contains(String name) {
        for (GameEntity entity : this.entities) {
            if (entity.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public GameEntity getEntity(String name) {
        for (GameEntity entity : this.entities) {
            if (entity.getName().equals(name)) {
                return entity;
            }
        }
        return null;
    }

    public HashSet<GameEntity> getEntities() {
        return this.entities;
    }

    public boolean hasEntities() {
        return !this.entities.isEmpty();
    }
}