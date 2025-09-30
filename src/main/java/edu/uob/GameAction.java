package edu.uob;

import java.util.List;

public class GameAction {
    private final List<String> triggers;
    private final List<String> subjects;
    private final List<String> consumed;
    private final List<String> produced;
    private String narration;

    public GameAction(List<String> triggers, List<String> subjects, List<String> consumed, List<String> produced, String narration) {
        this.triggers = List.copyOf(triggers);
        this.subjects = List.copyOf(subjects);
        this.consumed = List.copyOf(consumed);
        this.produced = List.copyOf(produced);
        this.narration = narration;
    }

    public List<String> getTriggers() {
        return this.triggers;
    }

    public List<String> getSubjects() {
        return this.subjects;
    }

    public List<String> getConsumed() {
        return this.consumed;
    }

    public List<String> getProduced() {
        return this.produced;
    }

    public String getNarration() {
        return this.narration;
    }
}