package model;

public enum PaymentMethod {
    CASH,
    CARD,
    TRANSFER,
    MOBILE_MONEY;

    @Override
    public String toString() {
        return name().replace("_", " ");
    }
}
