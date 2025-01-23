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

package app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

  @Autowired private MockMvc mockMvc; // MockMvc is used to perform HTTP requests in tests

  @MockBean private DataController dataController; // Mocked dependency of ItemController

  @Test
  @DisplayName("Test reading an item by ID")
  void testReadItem() throws Exception {
    // Arrange: DataController returns an Item for the provided ID
    long itemId = 1;
    Item item = new Item(1L, "ItemName", "ItemDescription", 100.0);

    given(dataController.get(itemId)).willReturn(item); // Simulate DataController behavior

    // Act: Perform GET /item/1
    mockMvc
        .perform(get("/api/item/{id}", itemId))
        .andExpect(status().isOk()) // Assert HTTP status is 200 OK
        .andExpect(
            content().string(Matchers.containsString("\"id\":1"))) // Assert JSON contains "id":1
        .andExpect(
            content()
                .string(
                    Matchers.containsString("\"name\":\"ItemName\""))) // Assert JSON contains name
        .andExpect(
            content()
                .string(
                    Matchers.containsString(
                        "\"description\":\"ItemDescription\""))) // Assert description
        .andExpect(content().string(Matchers.containsString("\"price\":100"))); // Assert price

    // Assert: Verify DataController's get method was called with the correct ID
    verify(dataController).get(itemId);
  }

  @Test
  @DisplayName("Test reading an item that does not exist")
  void testReadItem_NotFound() throws Exception {
    // Arrange: DataController returns null for the provided ID
    long itemId = 2;
    given(dataController.get(itemId)).willReturn(null); // Simulate item not found

    // Act: Perform GET /item/2
    mockMvc
        .perform(get("/api/item/{id}", itemId))
        .andExpect(status().isNotFound()); // Assert HTTP status is 404 Not Found

    // Assert: Verify DataController's get method was called
    verify(dataController).get(itemId);
  }

  @Test
  @DisplayName("Test reading multiple random items")
  void testReadMultipleItems() throws Exception {
    // Arrange: DataController returns a list of random items
    Item item1 = new Item(1L, "Item1", "Description1", 100.0);
    Item item2 = new Item(2L, "Item2", "Description2", 200.0);
    Item item3 = new Item(3L, "Item3", "Description3", 300.0);
    Item item4 = new Item(4L, "Item4", "Description4", 400.0);
    Item item5 = new Item(5L, "Item5", "Description5", 500.0);
    Item item6 = new Item(6L, "Item6", "Description6", 600.0);
    Item item7 = new Item(7L, "Item7", "Description7", 700.0);
    Item item8 = new Item(8L, "Item8", "Description8", 800.0);
    Item item9 = new Item(9L, "Item9", "Description9", 900.0);
    Item item10 = new Item(10L, "Item10", "Description10", 1000.0);

    List<Item> items =
        List.of(item1, item2, item3, item4, item5, item6, item7, item8, item9, item10);
    given(dataController.getMultiple(10)).willReturn(items);

    // Act: Perform GET /random
    mockMvc
        .perform(get("/api/item/random"))
        .andExpect(status().isOk()) // Assert HTTP status is 200 OK
        .andExpect(
            jsonPath("$.items.length()").value(10)) // Assert the `items` array has 10 elements
        .andExpect(jsonPath("$.items[0].id").value(1)) // Check first item's ID
        .andExpect(jsonPath("$.items[0].name").value("Item1")) // Check first item's name
        .andExpect(jsonPath("$.items[9].id").value(10)) // Check last item's ID
        .andExpect(jsonPath("$.items[9].name").value("Item10")); // Check last item's name

    // Assert: Verify DataController's getMultiple method was called with the correct parameter
    verify(dataController).getMultiple(10);
  }

  @Test
  @DisplayName("Test creating a new item")
  void testCreateItem() throws Exception {
    // Arrange: DataController successfully creates the item
    Item item = new Item("NewItem", "NewDescription", 200.0);

    given(dataController.create(any(Item.class))).willReturn(0L); // Simulate creation with ID 0

    // Act: Perform POST /item/create
    JSONObject itemJson = item.toJSONObject();
    itemJson.remove("id"); // Remove ID from JSON for creation
    mockMvc
        .perform(
            post("/api/item/create")
                .contentType("application/json") // Specify JSON content type
                .content(itemJson.toString()) // Convert JSON to string
            )
        .andExpect(status().isOk()) // Assert HTTP status is 200 OK
        .andExpect(content().string("{\"id\":0}")); // Assert response message

    // Assert: Verify DataController's create method was called with an argument matching the
    // expected properties
    verify(dataController)
        .create(
            argThat(
                argument ->
                    argument.getName().equals("NewItem")
                        && argument.getDescription().equals("NewDescription")
                        && argument.getPrice() == 200.0));
  }

  @Test
  @DisplayName("Test creating an item with invalid request body")
  void testCreateItem_Validations() throws Exception {
    // Act: Perform POST /item/create with an invalid request body (missing required fields)
    mockMvc
        .perform(
            post("/api/item/create")
                .contentType("application/json") // Specify JSON content type
                .content("{\"name\":\"NewItem\"}") // Incomplete JSON (missing fields)
            )
        .andExpect(status().isBadRequest()); // Assert HTTP status is 400 Bad Request

    // Assert: Verify DataController's create method was not called
    verify(dataController, never()).create(any());
  }

  @Test
  @DisplayName("Test deleting an item")
  void testDeleteItem() throws Exception {
    // Act: Perform DELETE /item/delete/5
    long itemId = 5;
    mockMvc
        .perform(delete("/api/item/delete/{id}", itemId))
        .andExpect(status().isOk()) // Assert HTTP status is 200 OK
        .andExpect(content().string("{\"id\":5}")); // Assert response message

    // Assert: Verify DataController's delete method was called with the correct ID
    verify(dataController).delete(itemId);
  }
}
