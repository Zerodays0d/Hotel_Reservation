package model;

public enum RoomType {
    SINGLE,
    DOUBLE,
    SUITE;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
