/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** Data class representing an item in the application. */
package app;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.json.JSONObject;

public class Item {

  private final Long id;

  @NotNull private final String name;

  @NotNull private final String description;

  @NotNull @Positive private final Double price;

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
        jsonObject.getDouble("price"));
  }
}
