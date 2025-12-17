package model;

public enum TaxiState {
    AVAILABLE("Доступен"),
    GOING_TO_CLIENT("Едет к клиенту"),
    TRANSPORTING("Везет пассажира"),
    OFFLINE("Оффлайн");

    private final String displayName;

    TaxiState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
