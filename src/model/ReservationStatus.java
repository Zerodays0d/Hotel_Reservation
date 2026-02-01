package model;


public enum ReservationStatus {
    BOOKED,
    CHECKED_IN,
    COMPLETED,
    CANCELLED;

    @Override
    public String toString() {
        String s = name().replace("_", " ");
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
