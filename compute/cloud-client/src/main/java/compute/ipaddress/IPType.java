package compute.ipaddress;

public enum IPType {
  INTERNAL("internal"),
  EXTERNAL("external"),
  IP_V6("ipv6");

  private final String type;

  IPType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
