package app;

public enum FromCacheType {
    FROM_DB(0),
    FULL_CACHE(1);

    private int value;

    FromCacheType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
