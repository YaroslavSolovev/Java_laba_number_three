package model;

public enum TaxiType {
    ECONOMY("Эконом", 1.0, 10.0),
    COMFORT("Комфорт", 1.5, 15.0),
    PREMIUM("Премиум", 2.0, 20.0);

    private final String displayName;
    private final double speedMultiplier;
    private final double basePricePerKm;

    TaxiType(String displayName, double speedMultiplier, double basePricePerKm) {
        this.displayName = displayName;
        this.speedMultiplier = speedMultiplier;
        this.basePricePerKm = basePricePerKm;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public double getBasePricePerKm() {
        return basePricePerKm;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
