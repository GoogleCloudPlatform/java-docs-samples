/**
 * Data class representing an item in the application.
 */

package app;

import org.json.JSONObject;

public class Item {

  private final Long id;
  private final String name;
  private final String description;
  private final Double price;
  private boolean fromCache;

  public Item() {
    this(null, "", "", null);
  }

  public Item(Long id, String name, String description, Double price) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.fromCache = false;
  }

  public Item(String name, String description, Double price) {
    this(null, name, description, price);
  }

  public Long getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getDescription() {
    return this.description;
  }

  public Double getPrice() {
    return this.price;
  }

  public boolean isFromCache() {
    return this.fromCache;
  }

  public void setFromCache(boolean fromCache) {
    this.fromCache = fromCache;
  }

  public JSONObject toJSONObject() {
    JSONObject obj = new JSONObject();
    obj.put("id", this.id);
    obj.put("name", this.name);
    obj.put("description", this.description);
    obj.put("price", this.price);
    obj.put("fromCache", this.fromCache);

    return obj;
  }

  public static Item fromJSONString(String obj) {
    JSONObject jsonObject = new JSONObject(obj);
    return new Item(
      jsonObject.getLong("id"),
      jsonObject.getString("name"),
      jsonObject.getString("description"),
      jsonObject.getDouble("price")
    );
  }
}
