package me.eccentric_nz.suggestionbox;

import java.util.HashMap;

public enum Type {

    SUGGESTION("Suggestion", "s"),
    COMMENT("Comment", "c"),
    QUESTION("Question", "q"),
    REQUEST("Request", "r");

    private static HashMap<String, Type> byArgument = new HashMap<>();

    static {
        for (Type t : values()) {
            byArgument.put(t.argument, t);
        }
    }

    private String name;
    private String argument;

    Type(String name, String argument) {
        this.name = name;
        this.argument = argument;
    }

    public static HashMap<String, Type> getByArgument() {
        return byArgument;
    }

    public String getName() {
        return name;
    }
}
