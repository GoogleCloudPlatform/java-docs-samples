package app;

public enum OrderByType {
  HIGH_TO_LOW("h2l"),
  LOW_TO_HIGH("l2h");

  private final String value;

  OrderByType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static OrderByType fromString(String text) {
    for (OrderByType b : OrderByType.values()) {
      if (b.value.equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }

  public static boolean isValid(String text) {
    return fromString(text) != null;
  }
}
