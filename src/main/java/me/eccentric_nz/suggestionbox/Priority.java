package me.eccentric_nz.suggestionbox;

public enum Priority {

    NORMAL(1),
    MEDIUM(2),
    HIGH(3);

    private int id;

    Priority(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
