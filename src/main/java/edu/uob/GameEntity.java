package edu.uob;

import java.util.HashMap;

public abstract class GameEntity
{
    private String name;
    private String description;
    private HashMap<String, String> attributes;

    public GameEntity(String name, String description)
    {
        this.name = name;
        this.description = description;
        this.attributes = new HashMap<>();
    }

    public void addAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public String getName()
    {
        return this.name;
    }
    public String getDescription()
    {
        return this.description;
    }

}
