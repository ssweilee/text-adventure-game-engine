package edu.uob;

import java.util.Iterator;
import java.util.LinkedList;

public class CommandProcessor {
    private final GameState gameState;
    private static final LinkedList<String> BUILT_IN_COMMANDS = new LinkedList<>();
    static {
        BUILT_IN_COMMANDS.add("inventory");
        BUILT_IN_COMMANDS.add("inv");
        BUILT_IN_COMMANDS.add("get");
        BUILT_IN_COMMANDS.add("drop");
        BUILT_IN_COMMANDS.add("goto");
        BUILT_IN_COMMANDS.add("look");
        BUILT_IN_COMMANDS.add("health");
    }

    public CommandProcessor(GameState gameState) {
        this.gameState = gameState;
    }

    public String processCommand(String command) {
        WholeCommand wholeCommand = this.parseCommand(command);
        if (wholeCommand == null) return "Missing valid trigger";

        Player player = this.checkPlayerExists(wholeCommand.playerName);
        CommandContext context = this.buildCommand(player, wholeCommand.commandWords);

        return this.processCommand(context);
    }

    private Player checkPlayerExists(String playerName) {
        Player player = this.gameState.getPlayer(playerName);
        if (player == null) {
            this.gameState.addPlayer(playerName);
            player = this.gameState.getPlayer(playerName);
        }
        return player;
    }

    private LinkedList<String> splitCommand(String commandText) {
        LinkedList<String> commandWords = new LinkedList<>();
        for (String commandWord : commandText.split("\\s+")) {
            if (!commandWord.isEmpty()) commandWords.add(commandWord);
        }
        return commandWords;
    }

    private WholeCommand parseCommand(String command) {
        int splitedIndex = command.indexOf(": ");

        String playerName = command.substring(0, splitedIndex);
        if (!playerName.matches("[a-zA-Z\\s'-]+")) return null;

        StringBuilder commandString = new StringBuilder();
        commandString.append(command.substring(splitedIndex + 2).trim().toLowerCase());
        String commandText = commandString.toString();

        LinkedList<String> commandWords = this.splitCommand(commandText);
        if (commandWords.isEmpty()) {
            return null;
        } else {
            return new WholeCommand(playerName, commandWords);
        }
    }

    private CommandContext buildCommand(Player player, LinkedList<String> commandWords) {
        LinkedList<String> allTriggers = this.gameState.getAllTriggers();
        LinkedList<String> allEntities = this.gatherAllEntities();
        LinkedList<String> allPlayerNames = this.gatherAllPlayerNames();
        LinkedList<String> allKeywords = this.buildAllKeywords(allTriggers, allEntities);
        String trigger = this.findTrigger(commandWords, allTriggers);
        LinkedList<String> subjects = this.extractSubjects(commandWords, trigger, allEntities);
        boolean hasPlayerName = this.isPlayerNameContained(commandWords, allPlayerNames);

        return new CommandContext(player, hasPlayerName, commandWords, trigger, subjects, allKeywords);
    }

    private String processCommand(CommandContext context) {
        if (context.trigger == null) return "No valid trigger found";

        if (BUILT_IN_COMMANDS.contains(context.trigger)) {
            return this.processBuiltInCommand(context);
        }
        return this.processCustomAction(context);
    }

    private String processBuiltInCommand(CommandContext context) {
        String trigger = context.trigger;
        if (trigger.equals("inv") || trigger.equals("inventory") || trigger.equals("look")) {
            if (!context.subjects.isEmpty()) {
                return this.buildErrorMessage(trigger, "takes no subjects");
            }
            if (trigger.equals("look")) return this.gameState.look(context.player.getName());
            return this.gameState.inventory(context.player.getName());
        } else if (trigger.equals("health")) {
            if (!context.subjects.isEmpty()) {
                return this.buildErrorMessage(trigger, "takes no subjects");
            }
            if (context.hasPlayerName) {
                return this.buildErrorMessage(trigger, "cannot with player names");
            }
            return String.valueOf(context.player.getHealth());
        } else if (trigger.equals("get") || trigger.equals("drop") || trigger.equals("goto")) {
            if (context.subjects.isEmpty()) return "no such item";
            if (context.subjects.size() > 1) {
                return this.buildErrorMessage(trigger, "allows only one subject");
            }
            String subject = context.subjects.getFirst();
            if (trigger.equals("get")) return this.gameState.getItem(context.player.getName(), subject);
            if (trigger.equals("drop")) return this.gameState.dropItem(context.player.getName(), subject);
            return this.gameState.gotoLocation(context.player.getName(), subject);
        }
        return "Unknown command";
    }

    private LinkedList<String> gatherAllEntities() {
        LinkedList<String> allEntities = new LinkedList<>();
        for (Location currentLocation: this.gameState.getLocations().values()) {
            this.addEntitiesFromLocation(currentLocation, allEntities);
        }
        for (Player player : this.gameState.getPlayers().values()) {
            allEntities.add(player.getName().toLowerCase());
            for (GameEntity item : player.getInventoryEntities()) {
                allEntities.add(item.getName().toLowerCase());
            }
        }
        allEntities.add("health");
        return allEntities;
    }

    //to gather all entities to identify available
    private void addEntitiesFromLocation(Location currentLocation, LinkedList<String> entities) {
        for (GameEntity artefact : currentLocation.getEntities("artefacts")) {
            entities.add(artefact.getName().toLowerCase());
        }
        for (GameEntity furniture : currentLocation.getEntities("furniture")) {
            entities.add(furniture.getName().toLowerCase());
        }
        for (GameEntity character : currentLocation.getEntities("characters")) {
            entities.add(character.getName().toLowerCase());
        }
        entities.add(currentLocation.getName().toLowerCase());
    }

    private LinkedList<String> gatherAllPlayerNames() {
        LinkedList<String> allPlayerNames = new LinkedList<>();
        for (Player player : this.gameState.getPlayers().values()) {
            allPlayerNames.add(player.getName().toLowerCase());
        }
        return allPlayerNames;
    }

    private LinkedList<String> buildAllKeywords(LinkedList<String> allTriggers, LinkedList<String> allEntities) {
        LinkedList<String> allKeywords = new LinkedList<>();
        Iterator<String> builtInIter = BUILT_IN_COMMANDS.iterator();
        while (builtInIter.hasNext()) {
            allKeywords.add(builtInIter.next());
        }
        allKeywords.addAll(allTriggers);
        allKeywords.addAll(allEntities);
        return allKeywords;
    }

    private String findTrigger(LinkedList<String> words, LinkedList<String> allTriggers) {
        for (String word : words) {
            if (BUILT_IN_COMMANDS.contains(word) || allTriggers.contains(word)) {
                return word;
            }
        }
        return null;
    }

    //to check if there are more than one subject
    private LinkedList<String> extractSubjects(LinkedList<String> words, String trigger, LinkedList<String> allEntities) {
        LinkedList<String> subjects = new LinkedList<>();
        Iterator<String> wordIter = words.iterator();
        while (wordIter.hasNext()) {
            String word = wordIter.next();
            if (!word.equals(trigger) && allEntities.contains(word)) {
                subjects.add(word);
            }
        }
        return subjects;
    }

    //check for not interact with other player
    private boolean isPlayerNameContained(LinkedList<String> words, LinkedList<String> allPlayerNames) {
        Iterator<String> wordIter = words.iterator();
        while (wordIter.hasNext()) {
            if (allPlayerNames.contains(wordIter.next())) {
                return true;
            }
        }
        return false;
    }

    private String processCustomAction(CommandContext context) {
        if (context.subjects.isEmpty() && !BUILT_IN_COMMANDS.contains(context.trigger)) {
            return this.buildErrorMessage(context.trigger, "must input at least one subject");
        }
        GameAction action = this.findMatchingAction(context.player, context.trigger, context.subjects);
        if (action == null) {
            return this.determineActionError(context.trigger, context.subjects);
        }
        return this.gameState.executeAction(context.player.getName(), action);
    }

    private String buildErrorMessage(String trigger, String reason) {
        StringBuilder message = new StringBuilder();
        message.append("Invalid command: '").append(trigger).append("' ").append(reason);
        return message.toString();
    }


    private String determineActionError(String trigger, LinkedList<String> subjects) {
        StringBuilder errorString = new StringBuilder();
        LinkedList<GameAction> possibleActions = this.gameState.getActionsByTrigger(trigger);
        if (possibleActions.isEmpty()) {
            errorString.append("No matching action found for '").append(trigger).append("'");
        }
        if (possibleActions.size() > 1) {
            errorString.append("Ambiguous command: multiple actions match '").append(trigger).append("'");
        } else {
            GameAction potentialAction = possibleActions.getFirst();
            LinkedList<String> requiredSubjects = new LinkedList<>(potentialAction.getSubjects());
            LinkedList<String> extraSubjects = new LinkedList<>(subjects);
            extraSubjects.removeAll(requiredSubjects);
            if (!extraSubjects.isEmpty()) {
                errorString.append("Invalid command: extraneous entities - ");
                Iterator<String> iter = extraSubjects.iterator();
                while (iter.hasNext()) {
                    errorString.append(iter.next());
                    if (iter.hasNext()) errorString.append(", ");
                }
            } else {
                errorString.append("Invalid command: subjects not available or missing");
            }
        }
        return errorString.toString();
    }

    // Helper method to find matching action
    private GameAction findMatchingAction(Player player, String trigger, LinkedList<String> subjects) {
        LinkedList<GameAction> matches = new LinkedList<>();
        Location currentLocation = gameState.getLocation(player.getLocation());
        LinkedList<GameAction> possibleActions = gameState.getActionsByTrigger(trigger);

        for (GameAction action : possibleActions) {
            if (isActionMatch(action, player, currentLocation, subjects)) {
                matches.add(action);
            }
        }

        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() > 1) return null; // Ambiguous command
        return matches.getFirst();
    }

    private boolean isActionMatch(GameAction action, Player player, Location location, LinkedList<String> subjects) {
        LinkedList<String> actionSubjects = new LinkedList<>(action.getSubjects());
        boolean atLeastOneSubjectPresent = false;
        for (String subject : subjects) {
            if (actionSubjects.contains(subject)) {
                atLeastOneSubjectPresent = true;
                break;
            }
        }
        if (!atLeastOneSubjectPresent) return false;

        boolean allSubjectsAvailable = true;
        for (String subject : actionSubjects) {
            if (!subjects.contains(subject) &&
                    !location.hasArtefact(subject) &&
                    !location.hasFurniture(subject) &&
                    !location.hasCharacter(subject) &&
                    !player.hasItem(subject)) {
                allSubjectsAvailable = false;
                break;
            }
        }
        if (!allSubjectsAvailable) return false;


        LinkedList<String> extraSubjects = new LinkedList<>(subjects);
        extraSubjects.removeAll(actionSubjects);
        return extraSubjects.isEmpty();
    }

    public class WholeCommand {
        public final String playerName;
        public final LinkedList<String> commandWords;

        public WholeCommand(String playerName, LinkedList<String> commandWords) {
            this.playerName = playerName;
            this.commandWords = commandWords;
        }
    }

    public class CommandContext {
        public final Player player;
        public final boolean hasPlayerName;
        public final LinkedList<String> commandWords;
        public final String trigger;
        public final LinkedList<String> subjects;
        public final LinkedList<String> allKeywords;

        public CommandContext(Player player, boolean hasPlayerName, LinkedList<String> commandWords, String trigger, LinkedList<String> subjects, LinkedList<String> allKeywords) {
            this.player = player;
            this.hasPlayerName = hasPlayerName;
            this.commandWords = commandWords;
            this.trigger = trigger;
            this.subjects = subjects;
            this.allKeywords = allKeywords;
        }
    }
}
