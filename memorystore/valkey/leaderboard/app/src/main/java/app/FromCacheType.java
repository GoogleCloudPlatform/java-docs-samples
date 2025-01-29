package app;

public enum FromCacheType {
  FROM_DB(0),
  PARTIAL_CACHE(1),
  FULL_CACHE(2);

  private int value;

  FromCacheType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
