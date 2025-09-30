package edu.uob;

import java.util.Iterator;
import java.util.LinkedList;

public class Location {
    private String name;
    private String description;
    private final EntityList artefacts;
    private final EntityList furniture;
    private final LinkedList<String> paths;
    private final EntityList characters;
    private LinkedList<Player> players;

    public Location(String name, String description) {
        this.name = name;
        this.description = description;
        this.artefacts = new EntityList();
        this.furniture = new EntityList();
        this.paths = new LinkedList<>();
        this.characters = new EntityList();
        this.players = new LinkedList<>();
    }

    public LinkedList<GameEntity> getEntities(String type) {
        switch (type) {
            case "artefacts": return new LinkedList<>(this.artefacts.getEntities());
            case "furniture": return new LinkedList<>(this.furniture.getEntities());
            case "characters": return new LinkedList<>(this.characters.getEntities());
            default: return new LinkedList<>();
        }
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public void addArtefact(GameEntity entity) {
        this.artefacts.add(entity);
    }

    public void removeArtefact(String name) {
        this.artefacts.remove(name);
    }

    public boolean hasArtefact(String name) {
        return this.artefacts.contains(name);
    }

    public String getArtefacts() {
        return this.buildDiscription(this.artefacts);
    }

    public void addFurniture(GameEntity entity) {
        this.furniture.add(entity);
    }

    public void removeFurniture(String name) {
        this.furniture.remove(name);
    }

    public boolean hasFurniture(String name) {
        return this.furniture.contains(name.toLowerCase());
    }

    public String getFurniture() {
        return this.buildDiscription(this.furniture);
    }

    public GameEntity getFurnitureEntity(String name) {
        return this.furniture.getEntity(name);
    }

    public void addPath(String name) {
        this.paths.add(name);
    }

    public void removePath(String name) {
        this.paths.remove(name);
    }

    public boolean hasPath(String name) {
        return this.paths.contains(name);
    }

    public String getPaths() {
        if (this.paths.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = this.paths.iterator();
        while (iterator.hasNext()) {
            String path = iterator.next();
            sb.append(path);
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public void addCharacter(GameEntity entity) {
        this.characters.add(entity);
    }

    public boolean hasCharacter(String name) {
        return this.characters.contains(name);
    }

    public String getCharacters() {
        return this.buildDiscription(this.characters);
    }

    public GameEntity getCharacterEntity(String name) {
        return this.characters.getEntity(name);
    }

    public void removeCharacter(String name) {
        this.characters.remove(name);
    }

    public GameEntity getArtefactEntity(String name) {
        return this.artefacts.getEntity(name);
    }


    private String buildDiscription(EntityList entityList) {
        if (!entityList.hasEntities()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<GameEntity> iterator = entityList.getEntities().iterator();
        while (iterator.hasNext()) {
            GameEntity entity = iterator.next();
            sb.append(entity.getDescription());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}