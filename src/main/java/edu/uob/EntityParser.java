package edu.uob;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.LinkedList;

public class EntityParser {
    private GameState gameState;

    public EntityParser(GameState gameState) {
        this.gameState = gameState;
    }

    public void parseEntities(File entitiesFile) {
        try {
            LinkedList<Graph> graphs = this.parseDocument(entitiesFile);
            if (graphs == null || graphs.isEmpty()) {
                throw new IllegalStateException("No graphs found in the file or parsing failed.");
            }
            this.parseSections(graphs.get(0));
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Error parsing entities file: %s", e.getMessage()), e);
        }
    }

    private LinkedList<Graph> parseDocument(File entitiesFile) throws Exception {
        Parser parser = new Parser();
        FileReader reader = new FileReader(entitiesFile);
        parser.parse(reader);
        return new LinkedList<>(parser.getGraphs());
    }

    private void parseSections(Graph wholeDocument) {
        LinkedList<Graph> sections = new LinkedList<>(wholeDocument.getSubgraphs());
        if (sections.size() > 1) {
            LinkedList<Graph> locationSection = new LinkedList<>(sections.get(0).getSubgraphs());
            this.parseLocations(locationSection);
            this.parseEntities(locationSection);
            this.parsePaths(new LinkedList<>(sections.get(1).getEdges()));

            this.parseLocations(locationSection);
            this.parseEntities(locationSection);
            this.parsePaths(new LinkedList<>(sections.get(1).getEdges()));
        }
    }

    private void parseLocations(LinkedList<Graph> locations) {
        Iterator<Graph> iterator = locations.iterator();
        while (iterator.hasNext()) {
            Graph locationGraph = iterator.next();
            String name = locationGraph.getNodes(false).get(0).getId().getId();
            String description = locationGraph.getNodes(false).get(0).getAttribute("description");
            this.gameState.addLocation(name, description);
        }
    }

    public GameState parseStartLocation(File entitiesFile) {
        try {
            LinkedList<Graph> graphs = this.parseDocument(entitiesFile);
            if (!graphs.isEmpty()) {
                Graph wholeDocument = graphs.get(0);
                LinkedList<Graph> sections = new LinkedList<>(wholeDocument.getSubgraphs());
                if (sections.size() > 1) {
                    LinkedList<Graph> locationSection = new LinkedList<>(sections.get(0).getSubgraphs());
                    if (!locationSection.isEmpty()) {
                        Graph firstLocationGraph = locationSection.getFirst();
                        String name = firstLocationGraph.getNodes(false).get(0).getId().getId();
                        String description = firstLocationGraph.getNodes(false).get(0).getAttribute("description");
                        GameState tempState = new GameState(name);
                        tempState.addLocation(name, description);
                        this.gameState = tempState;
                        return tempState;
                    }
                }
            }
            throw new IllegalStateException("Failed to parse start location from entities file");
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Error parsing start location: %s", e.getMessage()));
        }
    }

    private LinkedList<String> getReservedWords() {
        LinkedList<String> reservedWords = new LinkedList<>();
        reservedWords.add("inventory");
        reservedWords.add("look");
        reservedWords.add("inv");
        reservedWords.add("get");
        reservedWords.add("drop");
        reservedWords.add("goto");
        reservedWords.add("health");
        return reservedWords;
    }

    private void parseEntities(LinkedList<Graph> locations) {
        Iterator<Graph> locationIterator = locations.iterator();
        LinkedList<String> reservedWords = this.getReservedWords();
        while (locationIterator.hasNext()) {
            Graph locationGraph = locationIterator.next();
            String locationName = locationGraph.getNodes(false).get(0).getId().getId();
            Location loc = this.gameState.getLocation(locationName);
            if (loc != null) {
                this.parseEntitySubgraphs(locationGraph, loc, reservedWords);
            }
        }
    }

    private void parseEntitySubgraphs(Graph locationGraph, Location loc, LinkedList<String> reservedWords){
        LinkedList<Graph> entitySubgraphs = new LinkedList<>(locationGraph.getSubgraphs());
        Iterator<Graph> entityIterator = entitySubgraphs.iterator();
        while (entityIterator.hasNext()) {
            Graph entityGraph = entityIterator.next();
            String type = entityGraph.getId().getId();
            LinkedList<Node> nodes = new LinkedList<>(entityGraph.getNodes(false));
            Iterator<Node> nodeIterator = nodes.iterator();
            while (nodeIterator.hasNext()) {
                Node node = nodeIterator.next();
                this.processEntityNode(node, type, loc, reservedWords);
            }
        }
    }
    private void processEntityNode(Node node, String type, Location loc, LinkedList<String> reservedWords) {
        String name = node.getId().getId();
        String description = node.getAttribute("description");

        GameEntity entity = this.createEntity(type, name, description);
        if (entity != null) {
            this.addEntityToLocation(entity, type, loc);
            this.addAttributesToEntity(node, entity);
        }
    }

    private GameEntity createEntity(String type, String name, String description) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case "artefacts":
                return new Artefact(name, description);
            case "furniture":
                return new Furniture(name, description);
            case "characters":
                return new Character(name, description);
            default:
                throw new IllegalArgumentException("Unknown entity type");
        }
    }

    private void addEntityToLocation(GameEntity entity, String type, Location loc) {
        if (type == null || entity == null || loc == null) {
            return;
        }
        switch (type) {
            case "artefacts":
                loc.addArtefact(entity);
                break;
            case "furniture":
                loc.addFurniture(entity);
                break;
            case "characters":
                loc.addCharacter(entity);
                break;
            default:
                throw new IllegalArgumentException("Unknown entity type");
        }
    }

    private void addAttributesToEntity(Node node, GameEntity entity) {
        for (String attr : node.getAttributes().keySet()) {
            if (!attr.equals("description")) {
                entity.addAttribute(attr, node.getAttribute(attr));
            }
        }
    }

    private void parsePaths(LinkedList<Edge> paths) {
        Iterator<Edge> iterator = paths.iterator();
        while (iterator.hasNext()) {
            Edge path = iterator.next();
            String from = path.getSource().getNode().getId().getId();
            String to = path.getTarget().getNode().getId().getId();
            this.gameState.addPath(from, to);
        }
    }
}